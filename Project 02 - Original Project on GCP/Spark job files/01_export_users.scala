
val rawData = sc.textFile("gs://amislastfmrecsys01.appspot.com/lastfm-dataset-1K/userid-profile.tsv")

val file = "gs://amislastfmrecsys01.appspot.com/output_files/users.csv"

val header = rawData.first()
val filteredData = rawData.filter(_(0) != header(0))
filteredData.first

def convertUserId(tag: String):Int = {
	return tag.substring(5).toInt
}

val users = filteredData.map(s => (convertUserId(s.substring(0, 11)) + "," + s))

users.saveAsTextFile(file)
