package com.heming.cryptosentiment
package stream
import analysis.SentimentAnalyzer

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.model._
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBClientBuilder}
import twitter4j._
object TwitterStreamApp extends App {
  val twitterStream = new TwitterStreamFactory().getInstance()

  val dynamoDBClient: AmazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
    .withRegion(Regions.US_EAST_1)
    .build()

  val sentimentAnalyzer = new SentimentAnalyzer() // Create an instance of the SentimentAnalyzer class
  val listener = new StatusListener(dynamoDBClient, sentimentAnalyzer) // Pass it to the StatusListener

  class StatusListener(dynamoDBClient: AmazonDynamoDB, sentimentAnalyzer: SentimentAnalyzer) extends twitter4j.StatusListener {
    private var tweetCount=0
    private val maxTweets=1500
    override def onStatus(status: Status): Unit = {
      if (tweetCount < maxTweets) {
        tweetCount += 1
        val text = status.getText.toLowerCase
        // Get the sentiment of the tweet
        val sentiment = sentimentAnalyzer.getSentiment(text)

        val cryptocurrencies = List("bitcoin", "ethereum", "bnb", "xrp", "solana", "cardano", "dogecoin", "avalanche", "tron", "chainlink")

        cryptocurrencies.foreach { crypto =>
          if (text.contains(crypto)) {
            // Update DynamoDB with sentiment information
            val updateItemRequest = new UpdateItemRequest()
              .withTableName("CryptoMentions")
              .addKeyEntry("CryptoName", new AttributeValue().withS(crypto))
              .addAttributeUpdatesEntry(
                "MentionsCount",
                new AttributeValueUpdate()
                  .withAction(AttributeAction.ADD)
                  .withValue(new AttributeValue().withN("1"))
              )
              // Include sentiment information
              .addAttributeUpdatesEntry(
                "Sentiment",
                new AttributeValueUpdate()
                  .withAction(AttributeAction.PUT)
                  .withValue(new AttributeValue().withS(sentiment))
              )
            dynamoDBClient.updateItem(updateItemRequest)
          }
        }
      }
      else {
        twitterStream.shutdown()
      }
    }

    override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}

    override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {}

    override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {}

    override def onStallWarning(warning: StallWarning): Unit = {}

    override def onException(ex: Exception): Unit = {
      ex.printStackTrace()
    }
  }
  twitterStream.addListener(new StatusListener(dynamoDBClient, sentimentAnalyzer))
  val query = new FilterQuery()
  query.track("Bitcoin", "Ethereum", "BNB", "XRP", "Solana", "Cardano", "Dogecoin", "Avalanche", "TRON", "Chainlink") // Replace with your keywords of interest
  // Start listening to the filtered stream.
  twitterStream.filter(query)

  sys.addShutdownHook {
    twitterStream.shutdown()
  }
}