ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "RealTimeCryptoTrendX",
    libraryDependencies ++= Seq(
      // Twitter4J for Twitter API
      "org.twitter4j" % "twitter4j-core" % "4.0.7",
      "org.twitter4j" % "twitter4j-stream" % "4.0.7",
      // Stanford CoreNLP for sentiment analysis
      "edu.stanford.nlp" % "stanford-corenlp" % "4.5.5",
      // Stanford CoreNLP model files (English)
      "edu.stanford.nlp" % "stanford-corenlp" % "4.5.5" classifier "models",
      "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.12.606",
       "org.scalactic" %% "scalactic" % "3.2.17",
       "org.scalatest" %% "scalatest" % "3.2.17" % "test",
       "org.mockito" % "mockito-core" % "5.8.0",
       "org.scalatestplus" %% "mockito-4-11" % "3.2.17.0" % "test",
       "org.mockito" % "mockito-inline" % "5.2.0"
    ),
    idePackagePrefix := Some("com.heming.cryptosentiment")

  )
