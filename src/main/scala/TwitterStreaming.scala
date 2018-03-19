import java.util.UUID

import com.datastax.spark.connector.SomeColumns
import com.datastax.spark.connector.streaming._
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Durations, Seconds, StreamingContext}

/**
  * Created by vsinha on 3/14/2018.
  */
object TwitterStreaming {

  def main(args: Array[String]) {

    //System.setProperty("hadoop.home.dir", "C:\\hadoop")

    if (args.length < 4) {
      System.err.println("Usage: TwitterStreaming <consumer key> <consumer secret> <access token> <access token secret> [<filters>]")
      System.exit(1)
    }

    val Array(consumerKey, consumerSecret, accessToken, accessTokenSecret) = args.take(4)
    val filters = args.takeRight(args.length - 4)

    // Set the system properties so that Twitter4j library used by twitter stream
    // Use them to generate OAuth credentials
    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)

    val sparkConf = new SparkConf()
      .setAppName("TwitterSentiment")
      //.setMaster("local[2]")
      .set("spark.cassandra.connection.host", "10.25.40.118")
      .set("spark.cassandra.connection.connections_per_executor_max", "2")


    val sc = new SparkContext(sparkConf)

    val ssc = new StreamingContext(sc, Seconds(1))

    // set the log level
    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)

    val stream = TwitterUtils.createStream(ssc, None, filters)
    val sentimentAnalyzer = new SentimentAnalyzer();

    // Parse the tweets
    val tweetWithSentiment = stream.map(_.getText)
      .map(r => {
        val sentiment = sentimentAnalyzer.analyze(r);
        (sentiment, r)
      })

    // Save to Cassandra
    tweetWithSentiment
      .map(rdd => (UUID.randomUUID(), rdd._1, rdd._2))
      .saveToCassandra("spark", "sentiments", SomeColumns("id", "sentiment", "tweet"))

    //tweetWithSentiment.print()

    // Count the numbers of each sentiment
//    tweetWithSentiment
//      .map(r => (r._1, 1))
//      .reduceByKeyAndWindow((x: Int, y: Int) => x + y, Durations.seconds(1), Durations.seconds(1))
//      .foreachRDD(r => r.foreach(s => println(s)))

    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate
  }

}
