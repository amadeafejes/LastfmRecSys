
val rawData = sc.textFile("userid-profile.tsv")

val file = "users.tsv"

val header = rawData.first()
val filteredData = rawData.filter(_(0) != header(0))
filteredData.first

def convertUserId(tag: String):Int = {
    return tag.substring(5).toInt
}

val users = filteredData.map(s => (convertUserId(s.substring(0, 11)) + "\t" + s))

users.saveAsTextFile(file)