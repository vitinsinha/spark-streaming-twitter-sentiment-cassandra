name := "spark-streaming-twitter-sentiment-cassandra"

version := "1.0"

scalaVersion := "2.11.8"

resolvers  += "MavenCentralRepository" at "http://central.maven.org/maven2"
resolvers  += "MavenCassandraRepository" at "https://mvnrepository.com/artifact/com.datastax.spark/spark-cassandra-connector"

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.5.2" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp"))
libraryDependencies += "org.apache.bahir" %% "spark-streaming-twitter" % "2.0.0"
libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.7"

libraryDependencies ++= {
  val sparkVer = "2.2.1"
  Seq(
    "org.apache.spark" %% "spark-core" % sparkVer % "provided",
    "org.apache.spark" %% "spark-streaming" % sparkVer % "provided",
    "org.apache.spark" %% "spark-sql" % sparkVer % "provided"
  )
}

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
    