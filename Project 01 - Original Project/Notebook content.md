# Notebook code in a readable format

## 01 - Export users

```scala
//read users file
val rawData = sc.textFile("hdfs://127.0.0.1/user/cloudera/lastfm_input/userid-profile.tsv")
//output file
val file = "hdfs://127.0.0.1/user/cloudera/lastfm_output/users.tsv"
//filter header
val header = rawData.first()
val filteredData = rawData.filter(_(0) != header(0))
filteredData.first

//method for user id
def convertUserId(tag: String):Int = {
    //get int from format "user_000758" - last 6 characters
    return tag.substring(5).toInt
}
//insert index for the users
val users = filteredData.map(s => (convertUserId(s.substring(0, 11)) + "\t" + s))
users.take(5)

//save rdd into text file
users.saveAsTextFile(file)
```

## 02 - Export unique tracks and listenings

```scala
//input file
val rawListenings = sc.textFile("hdfs://127.0.0.1/user/cloudera/lastfm_input/userid-timestamp-artid-artname-traid-traname.tsv")

//select columns from file and filter
case class Listening(userid:String,timestamp:String,artid:String,artname:String,traid:String,traname:String)
val listeningObjects = rawListenings.map(_.split("\t")).map(s => Listening(s(0),s(1),s(2),s(3),s(4),s(5))).filter(_.traid != "")
listeningObjects.first()
listeningObjects.count

//create indexed unique tracks
val uniqueTracksMap = listeningObjects.map(s => (s.traid, s)).combineByKey(
  (value) => List(value), //when a key is found for the first time in a given partition, creates an initial value for that key
  (aggr: List[Listening], value) => aggr ::: (value :: Nil), //is called when the key already has an accumulator
  (aggr1: List[Listening], aggr2: List[Listening]) => aggr1 ::: aggr2
).zipWithUniqueId.filter(_._1._2 != null).map( r => (r._2, r._1._2) )
uniqueTracksMap.first()
uniqueTracksMap.count

//create unique tracks
val uniqueTracks = uniqueTracksMap.map( r => (r._1, r._2(0)) ).map( r => (r._1 + "\t" + r._2.traid + "\t" + r._2.traname + "\t" + r._2.artid + "\t" + r._2.artname) )
uniqueTracks.first()
//save unique tracks into file
val tracksFile = "hdfs://127.0.0.1/user/cloudera/lastfm_output/unique_tracks.tsv"
uniqueTracks.saveAsTextFile(tracksFile)

//method for user id
def convertUserId(tag: String):Int = {
    //get int from format "user_000758" - last 6 characters
    return tag.substring(5).toInt
}
//listenings with integer user id and integer track id
val indexedListenings = 
    uniqueTracksMap.map( r => (r._2.map(e =>  
    (r._1 + "\t" + convertUserId(e.userid) + "\t" + e.timestamp)) ) )
    
//save listenings into file
val listeningsFile = "hdfs://127.0.0.1/user/cloudera/lastfm_output/listenings.tsv"
val listeningsToSave = indexedListenings.flatMap(r => r)
listeningsToSave.count
listeningsToSave.saveAsTextFile(listeningsFile)
```

## 03 - Export favorite tracks

```scala
//read tracks file
val rawListenings = sc.textFile("hdfs://127.0.0.1/user/cloudera/lastfm_output/listenings.tsv")
rawListenings.first

val n = 10
case class SimpleListening(userid:String, traid:String, timestamp:String)
val rawTracks = rawListenings.map(_.split("\t")).map(s => SimpleListening(s(1), s(0), s(2)))
rawTracks.first
rawTracks.count
rawTracks.take(5)

val userTrackPairs = rawTracks.map( r => (r.userid, r.traid) )
userTrackPairs.take(5)

//count unique user-track occurences
val countedTrackMap = userTrackPairs.map(s => (s,1))
countedTrackMap.count
countedTrackMap.take(20)

val countedTracks = countedTrackMap.reduceByKey(_ + _)
countedTracks.count
countedTracks.take(20)

//generate top n songs for all user
val topNListenedTracksForUsers = countedTracks.map(s => (s._1._1, (s._1._2, s._2))).groupByKey().mapValues( iter => iter.toList.sortBy(_._2)(Ordering[Int].reverse).take(n) ).flatMapValues(x => x)
topNListenedTracksForUsers.take(20)
topNListenedTracksForUsers.count

val topNListenedTracksForUsersFormatted = topNListenedTracksForUsers.map(  x => ( x._1 + "\t" + x._2._1 + "\t" + x._2._2 ) )
topNListenedTracksForUsersFormatted.take(20)
topNListenedTracksForUsersFormatted.count

//save fav. songs into file 
val favsFile = "hdfs://127.0.0.1/user/cloudera/lastfm_output/fav_tracks.tsv"
topNListenedTracksForUsersFormatted.saveAsTextFile(favsFile)
```

## 04 - Export recommended tracks

```scala
//read tracks file
val rawListenings = sc.textFile("hdfs://127.0.0.1/user/cloudera/lastfm_output/listenings.tsv")

//count unique user-track occurences
case class SimpleListening(userid:String, traid:String, timestamp:String)
val rawTracks = rawListenings.map(_.split("\t")).map(s => SimpleListening(s(1), s(0), s(2)))
val userTrackPairs = rawTracks.map( r => (r.userid, r.traid) )
val countedTrackMap = userTrackPairs.map(s => (s,1))
val countedTracks = countedTrackMap.reduceByKey(_ + _)
val ratingData = countedTracks.map(s => (s._1._1, s._1._2, s._2))

//import ALS and Rating from MLlib
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.Rating

//create rating object from tuple with hash values of id-s
val ratings = ratingData.map(t => Rating(t._1.toInt, t._2.toInt, t._3.toDouble))
//split data into training and test set
val splits = ratings.randomSplit(Array(0.8, 0.2))
val trainRatings = splits(0)
val testRatings = splits(1)

//import RDD
import org.apache.spark.rdd.RDD
//method to create model and count rmse for a given Rating rdd
def countRMSE( ratings:RDD[Rating], rank:Int, iterations:Int, lambda:Double, alpha:Double) : Double = {

//create a model
val model = ALS.trainImplicit(ratings, rank, iterations, lambda, alpha)
    
 val usersProducts = ratings.map { case Rating(user, product, rate) =>
  (user, product)
}
val predictions =
  model.predict(usersProducts).map { case Rating(user, product, rate) =>
    ((user, product), rate)
  }
val ratesAndPreds = ratings.map { case Rating(user, product, rate) =>
  ((user, product), rate)
}.join(predictions)
val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) =>
  val err = (r1 - r2)
  err * err
}.mean()

val RMSE = math.sqrt(MSE)

return RMSE

}

//default parameters for the function
val rank = 50
val iterations = 10
val lambda = 0.01
val alpha = 1.0

//function for counting more error and rmse between the given parameter ranges
def countRMSEErrorInBlock(rankMin:Double, rankMax:Double, iterationsMin:Double, iterationsMax:Double, lambdaMin:Double, lambdaMax:Double, alphaMin:Double, alphaMax:Double){

    val rankStep = 20
    val iterationsStep = 2
    val lambdaStep = 0.002
    val alphaStep = 0.1
	
	for (r <- rankMin to rankMax by rankStep){
        for (i <- iterationsMin to iterationsMax by iterationsStep){
            for (l <- lambdaMin to lambdaMax by lambdaStep){
                for (a <- alphaMin to alphaMax by alphaStep){
                    //call rmse method
                    val rmse = countRMSE(ratings, r.toInt, i.toInt, l, a)
                    val rmseTrain = countRMSE(trainRatings, r.toInt, i.toInt, l, a)
                    val rmseTest = countRMSE(testRatings, r.toInt, i.toInt, l, a)
                    val error = math.abs(rmseTrain - rmseTest)
                    
                    val resultString = error + "\t" + rmse + "\t" + r + "\t" + i + "\t" + l + "\t" + a
                    
                    println(resultString)
				}
            }
        }
    }
}

//example calls of the above function
//only rank changes
countRMSEErrorInBlock(10, 120, iterations, iterations, lambda, lambda, alpha, alpha)
//only iterations changes
countRMSEErrorInBlock(rank, rank, 6, 17, lambda, lambda, alpha, alpha)
//only lambda changes
countRMSEErrorInBlock(rank, rank, iterations, iterations, 0.002, 0.024, alpha, alpha)
//only alpha changes
countRMSEErrorInBlock(rank, rank, iterations, iterations, lambda, lambda, 0.4, 1.5)


////generate top k recommendations for all users and save the results into files

val model = ALS.trainImplicit(ratings, rank, iterations, lambda, alpha)

val ratingValues = model.userFeatures.map( r => r._2 ).flatMap( r => r)
val maxValue = ratingValues.max()
val avgValue = ratingValues.sum/ratingValues.count
val percentageUpper = (maxValue - avgValue)/50
val percentageLower = avgValue/50

val userIDs = ratingData.map(t => t._1.toInt ).distinct()
userIDs.count
userIDs.take(5)
val K=10
val folder = "hdfs://127.0.0.1/user/cloudera/lastfm_output/rec_tracks/"
val fileExt = ".tsv"

val recs =  userIDs.collect().foreach(
      
      userID => sc.parallelize( model.recommendProducts(userID, K).map(r => (r.user + "\t" + r.product + "\t" + 
      (
      //count percentage value from rating
      if(r.rating >= avgValue) (50 + (r.rating/percentageUpper))
      else (50 - (r.rating/percentageLower))
      )
       ) ) ).saveAsTextFile(folder + userID + fileExt)
    
)
```
