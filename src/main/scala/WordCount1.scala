import java.sql.Timestamp
import org.apache.spark.sql.SparkSession

object WordCount1 {

  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      System.err.println ( "Usage: AkkaStreamWordCount <urlOfPublisher>" ) // scalastyle:off println
      System.exit ( 1 )
    }

    println("prining"+args(0))
    val urlOfPublisher = args(0)



    val spark = SparkSession
      .builder()
      .appName("AkkaStreamWordCount")
      .master("local[4]")
      .getOrCreate()

    import spark.implicits._

    val lines = spark.readStream
      .format("org.apache.bahir.sql.streaming.akka.AkkaStreamSourceProvider")
      .option("urlOfPublisher", urlOfPublisher)
      .load().as[(String, Timestamp)]

    // Split the lines into words
    val words = lines.map(_._1).flatMap(_.split(" "))

    // Generate running word count
    val wordCounts = words.groupBy("value").count()

    // Start running the query that prints the running counts to the console
    val query = wordCounts.writeStream
      .outputMode("complete")
      .format("console")
      .start()

    query.awaitTermination()
  }

}
