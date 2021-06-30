scalaVersion     := "2.13.5"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "ua.edu.ucu"
ThisBuild / organizationName := "ucu"

resolvers ++= Seq(
    "confluent" at "https://packages.confluent.io/maven/",
    Resolver.mavenLocal
)

lazy val root = (project in file("."))
  .settings(
    name := "keyword-detection-service",
    libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "2.8.0",
    libraryDependencies += "com.sksamuel.avro4s" %% "avro4s-core" % "4.0.7",
    libraryDependencies += "com.sksamuel.avro4s" %% "avro4s-kafka" % "4.0.7",
    libraryDependencies += "org.apache.avro" % "avro" % "1.10.2",
    libraryDependencies += "io.confluent" % "kafka-streams-avro-serde" % "6.2.0",
  )
