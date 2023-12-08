package com.heming.cryptosentiment
package stream
import analysis.SentimentAnalyzer

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import twitter4j.Status

class TwitterStreamAppTest extends AnyFlatSpec with Matchers {

  val cryptocurrencies: List[String] = List("bitcoin", "ethereum", "bnb", "xrp", "solana", "cardano", "dogecoin", "avalanche", "tron", "chainlink")

  cryptocurrencies.foreach { crypto =>
    s"onStatus with $crypto mention" should "update DynamoDB" in {
      val status = mock(classOf[Status])
      val dynamoDBClient = mock(classOf[AmazonDynamoDB])
      val sentimentAnalyzer = mock(classOf[SentimentAnalyzer])
      val listener = new TwitterStreamApp.StatusListener(dynamoDBClient, sentimentAnalyzer)

      when(status.getText).thenReturn(s"$crypto is on the rise!")
      listener.onStatus(status)

      verify(dynamoDBClient).updateItem(any[UpdateItemRequest])
    }
  }

  "onStatus with no cryptocurrency mention" should "not update DynamoDB" in {
    val status = mock(classOf[Status])
    val dynamoDBClient = mock(classOf[AmazonDynamoDB])
    val sentimentAnalyzer = mock(classOf[SentimentAnalyzer])
    val listener = new TwitterStreamApp.StatusListener(dynamoDBClient, sentimentAnalyzer)

    when(status.getText).thenReturn("This tweet does not mention any cryptocurrencies.")
    listener.onStatus(status)

    verify(dynamoDBClient, never()).updateItem(any[UpdateItemRequest])
  }

  "onStatus with multiple cryptocurrency mentions" should "update DynamoDB for each mention" in {
    val status = mock(classOf[Status])
    val dynamoDBClient = mock(classOf[AmazonDynamoDB])
    val sentimentAnalyzer = mock(classOf[SentimentAnalyzer])
    val listener = new TwitterStreamApp.StatusListener(dynamoDBClient, sentimentAnalyzer)

    when(status.getText).thenReturn("bitcoin and ethereum are both on the rise!")
    listener.onStatus(status)

    verify(dynamoDBClient, times(1)).updateItem(argThat((argument: UpdateItemRequest) => {
      argument.getKey.get("CryptoName").getS == "bitcoin"
    }))
    verify(dynamoDBClient, times(1)).updateItem(argThat((argument: UpdateItemRequest) => {
      argument.getKey.get("CryptoName").getS == "ethereum"
    }))
  }


  "onException" should "print the stack trace of the exception" in {
    val exception = mock(classOf[Exception])
    val dynamoDBClient = mock(classOf[AmazonDynamoDB])
    val sentimentAnalyzer = mock(classOf[SentimentAnalyzer])
    val listener = new TwitterStreamApp.StatusListener(dynamoDBClient, sentimentAnalyzer)

    doNothing().when(exception).printStackTrace()
    listener.onException(exception)

    verify(exception).printStackTrace()
  }

  "onStatus with positive sentiment" should "update DynamoDB with Positive sentiment" in {
    val status = mock(classOf[Status])
    val dynamoDBClient = mock(classOf[AmazonDynamoDB])
    val sentimentAnalyzer = mock(classOf[SentimentAnalyzer])
    val listener = new TwitterStreamApp.StatusListener(dynamoDBClient, sentimentAnalyzer)

    when(status.getText).thenReturn("bitcoin is on the rise, great news!")
    when(sentimentAnalyzer.getSentiment("bitcoin is on the rise, great news!")).thenReturn("Positive")

    listener.onStatus(status)

    verify(dynamoDBClient, times(1)).updateItem(argThat[UpdateItemRequest] { argument =>
      val attributes = argument.getAttributeUpdates
      attributes.containsKey("Sentiment") && attributes.get("Sentiment").getValue.getS == "Positive"
    })
  }

  "onStatus with neutral sentiment" should "update DynamoDB with Neutral sentiment" in {
    val status = mock(classOf[Status])
    val dynamoDBClient = mock(classOf[AmazonDynamoDB])
    val sentimentAnalyzer = mock(classOf[SentimentAnalyzer])
    val listener = new TwitterStreamApp.StatusListener(dynamoDBClient, sentimentAnalyzer)

    when(status.getText).thenReturn("bitcoin is being discussed.")
    when(sentimentAnalyzer.getSentiment("bitcoin is being discussed.")).thenReturn("Neutral")

    listener.onStatus(status)

    verify(dynamoDBClient, times(1)).updateItem(argThat[UpdateItemRequest] { argument =>
      val attributes = argument.getAttributeUpdates
      attributes.containsKey("Sentiment") && attributes.get("Sentiment").getValue.getS == "Neutral"
    })
  }

  "onStatus with negative sentiment" should "update DynamoDB with Negative sentiment" in {
    val status = mock(classOf[Status])
    val dynamoDBClient = mock(classOf[AmazonDynamoDB])
    val sentimentAnalyzer = mock(classOf[SentimentAnalyzer])
    val listener = new TwitterStreamApp.StatusListener(dynamoDBClient, sentimentAnalyzer)

    when(status.getText).thenReturn("bitcoin is crashing, terrible news!")
    when(sentimentAnalyzer.getSentiment("bitcoin is crashing, terrible news!")).thenReturn("Negative")

    listener.onStatus(status)

    verify(dynamoDBClient, times(1)).updateItem(argThat[UpdateItemRequest] { argument =>
      val attributes = argument.getAttributeUpdates
      attributes.containsKey("Sentiment") && attributes.get("Sentiment").getValue.getS == "Negative"
    })
  }




}