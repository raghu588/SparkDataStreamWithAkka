name := "Test"

version := "0.1"

scalaVersion := "2.11.0"




// https://mvnrepository.com/artifact/org.apache.bahir/spark-sql-streaming-akka
libraryDependencies += "org.apache.bahir" %% "spark-sql-streaming-akka" % "2.1.1"

// https://mvnrepository.com/artifact/org.apache.spark/spark-streaming
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.0.0" % "provided"


// https://mvnrepository.com/artifact/org.apache.bahir/spark-streaming-akka
libraryDependencies += "org.apache.bahir" %% "spark-streaming-akka" % "2.0.0-preview"

// https://mvnrepository.com/artifact/org.rocksdb/rocksdbjni
libraryDependencies += "org.rocksdb" % "rocksdbjni" % "5.9.2"
