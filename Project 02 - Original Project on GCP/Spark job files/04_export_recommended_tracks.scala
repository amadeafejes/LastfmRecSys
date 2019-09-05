
val rawListenings = sc.textFile("gs://amislastfmrecsys01.appspot.com/output_files/listenings.tsv")

case class SimpleListening(userid:String, traid:String, timestamp:String)
val rawTracks = rawListenings.map(_.split("\t")).map(s => SimpleListening(s(1), s(0), s(2)))
val userTrackPairs = rawTracks.map( r => (r.userid, r.traid) )
val countedTrackMap = userTrackPairs.map(s => (s,1))
val countedTracks = countedTrackMap.reduceByKey(_ + _)
val ratingData = countedTracks.map(s => (s._1._1, s._1._2, s._2))

import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.Rating

val ratings = ratingData.map(t => Rating(t._1.toInt, t._2.toInt, t._3.toDouble))

val splits = ratings.randomSplit(Array(0.8, 0.2))
val trainRatings = splits(0)
val testRatings = splits(1)

import org.apache.spark.rdd.RDD

def countRMSE( ratings:RDD[Rating], rank:Int, iterations:Int, lambda:Double, alpha:Double) : Double = {

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

val rank = 50
val iterations = 10
val lambda = 0.01
val alpha = 1.0

def countRMSEErrorInBlock(rankMin:Double, rankMax:Double, iterationsMin:Double, iterationsMax:Double, lambdaMin:Double, lambdaMax:Double, alphaMin:Double, alphaMax:Double){

    val rankStep = 20
    val iterationsStep = 2
    val lambdaStep = 0.002
    val alphaStep = 0.1
	
	for (r <- rankMin to rankMax by rankStep){
        for (i <- iterationsMin to iterationsMax by iterationsStep){
            for (l <- lambdaMin to lambdaMax by lambdaStep){
                for (a <- alphaMin to alphaMax by alphaStep){
                    
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

countRMSEErrorInBlock(10, 120, iterations, iterations, lambda, lambda, alpha, alpha)

countRMSEErrorInBlock(rank, rank, 6, 17, lambda, lambda, alpha, alpha)

countRMSEErrorInBlock(rank, rank, iterations, iterations, 0.002, 0.024, alpha, alpha)

countRMSEErrorInBlock(rank, rank, iterations, iterations, lambda, lambda, 0.4, 1.5)

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
val folder = "gs://amislastfmrecsys01.appspot.com/output_files/rec_tracks"
val fileExt = ".tsv"

val recs =  userIDs.collect().foreach(
      
      userID => sc.parallelize( model.recommendProducts(userID, K).map(r => (r.user + "\t" + r.product + "\t" + 
      (
      if(r.rating >= avgValue) (50 + (r.rating/percentageUpper))
      else (50 - (r.rating/percentageLower))
      )
       ) ) ).saveAsTextFile(folder + userID + fileExt)
    
)