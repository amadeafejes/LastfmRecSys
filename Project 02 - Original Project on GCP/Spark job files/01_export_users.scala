
val rawData = sc.textFile("gs://amislastfmrecsys01.appspot.com/lastfm-dataset-1K/userid-profile.tsv")

val file = "gs://amislastfmrecsys01.appspot.com/output_files/users.csv"

val header = rawData.first()
val filteredData = rawData.filter(_(0) != header(0))
filteredData.first

case class User(id:String,gender:String,age:Int,country:String,registered:String)
val userObjects = filteredData.map(_.split("\t")).map(s => User(s(0),s(1),s(2),s(3),s(4))

def convertUserId(tag: String):Int = {
	return tag.substring(5).toInt
}

val users = userObjects.map(s => (convertUserId(s(0))+ "," + s(0) + "," + s(1) + "," + s(2) + "," + s(3) + "," + '"' + s(4) + '"'))

users.saveAsTextFile(file)
