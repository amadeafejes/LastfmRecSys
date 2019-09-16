val usersFile = "gs://amislastfmrecsys01.appspot.com/output_files/users.csv"
val users = sc.textFile(usersFile)
val folder = "gs://amislastfmrecsys01.appspot.com/output_files"

val userIds = users.map(_.split(",")).map(s => s(0)).collect.toList

var concatenatedRecTracks = sc.emptyRDD[String]

for (userId <- userIds){
val path = folder + "/rec_tracks" + userId + ".csv"
val piece = sc.textFile(path)
	concatenatedRecTracks = concatenatedRecTracks.union(piece)
}

val concatenatedFile = "gs://amislastfmrecsys01.appspot.com/output_files/rec_tracks.csv"
concatenatedRecTracks.saveAsTextFile(concatenatedFile)
