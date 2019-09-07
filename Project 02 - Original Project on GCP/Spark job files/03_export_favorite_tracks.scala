
val rawListenings = sc.textFile("gs://amislastfmrecsys01.appspot.com/output_files/listenings.csv")
rawListenings.first

val n = 10
case class SimpleListening(userid:String, traid:String, timestamp:String)
val rawTracks = rawListenings.map(_.split(",")).map(s => SimpleListening(s(1), s(0), s(2)))
rawTracks.first
rawTracks.count
rawTracks.take(5)

val userTrackPairs = rawTracks.map( r => (r.userid, r.traid) )
userTrackPairs.take(5)

val countedTrackMap = userTrackPairs.map(s => (s,1))
countedTrackMap.count
countedTrackMap.take(20)

val countedTracks = countedTrackMap.reduceByKey(_ + _)
countedTracks.count
countedTracks.take(20)

val topNListenedTracksForUsers = countedTracks.map(s => (s._1._1, (s._1._2, s._2))).groupByKey().mapValues( iter => iter.toList.sortBy(_._2)(Ordering[Int].reverse).take(n) ).flatMapValues(x => x)
topNListenedTracksForUsers.take(20)
topNListenedTracksForUsers.count

val topNListenedTracksForUsersFormatted = topNListenedTracksForUsers.map(  x => ( x._1 + "," + x._2._1 + "," + x._2._2 ) )
topNListenedTracksForUsersFormatted.take(20)
topNListenedTracksForUsersFormatted.count
 
val favsFile = "gs://amislastfmrecsys01.appspot.com/output_files/fav_tracks.csv"
topNListenedTracksForUsersFormatted.saveAsTextFile(favsFile)