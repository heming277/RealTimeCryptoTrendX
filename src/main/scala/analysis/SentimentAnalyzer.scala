package com.heming.cryptosentiment
package analysis
import edu.stanford.nlp.pipeline.{CoreDocument, StanfordCoreNLP}
import java.util.Properties
class SentimentAnalyzer {
  private val props = new Properties()
  props.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
  private val pipeline = new StanfordCoreNLP(props)

  def getSentiment(text: String): String = {
    val document = new CoreDocument(text)
    pipeline.annotate(document)
    val sentences = document.sentences()
    val sentiment = if (sentences.isEmpty) "Neutral" else sentences.get(0).sentiment()
    sentiment
  }
}

// This is the companion object that holds the singleton instance.
