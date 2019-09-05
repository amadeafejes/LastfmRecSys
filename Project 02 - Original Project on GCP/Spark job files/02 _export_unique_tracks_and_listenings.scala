
val rawListenings = sc.textFile("gs://amislastfmrecsys01.appspot.com/lastfm-dataset-1K/userid-timestamp-artid-artname-traid-traname.tsv")

case class Listening(userid:String,timestamp:String,artid:String,artname:String,traid:String,traname:String)
val listeningObjects = rawListenings.map(_.split("\t")).map(s => Listening(s(0),s(1),s(2),s(3),s(4),s(5))).filter(_.traid != "")
listeningObjects.first()
listeningObjects.count

val uniqueTracksMap = listeningObjects.map(s => (s.traid, s)).combineByKey(
  (value) => List(value),
  (aggr: List[Listening], value) => aggr ::: (value :: Nil),
  (aggr1: List[Listening], aggr2: List[Listening]) => aggr1 ::: aggr2
).zipWithUniqueId.filter(_._1._2 != null).map( r => (r._2, r._1._2) )
uniqueTracksMap.first()
uniqueTracksMap.count

val uniqueTracks = uniqueTracksMap.map( r => (r._1, r._2(0)) ).map( r => (r._1 + "\t" + r._2.traid + "\t" + r._2.traname + "\t" + r._2.artid + "\t" + r._2.artname) )
uniqueTracks.first()

val tracksFile = "unique_tracks.tsv"
uniqueTracks.saveAsTextFile(tracksFile)

def convertUserId(tag: String):Int = {
    return tag.substring(5).toInt
}

val indexedListenings = 
    uniqueTracksMap.map( r => (r._2.map(e =>  
    (r._1 + "\t" + convertUserId(e.userid) + "\t" + e.timestamp)) ) )
    

val listeningsFile = "gs://amislastfmrecsys01.appspot.com/output_files/listenings.tsv"
val listeningsToSave = indexedListenings.flatMap(r => r)
listeningsToSave.count
listeningsToSave.saveAsTextFile(listeningsFile)