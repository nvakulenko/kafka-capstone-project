package ua.edu.ucu

object Main extends App {
  val kafkaBrokerURL = sys.env.get("KAFKA_BROKER_URL").getOrElse("localhost:29092")
  val schemaRegistryURL = sys.env.get("SCHEMA_REGISTRY_URL").getOrElse("http://localhost:8081")
  val applicationId = sys.env.get("APPLICATION_ID").getOrElse("keyword-detection-service")
  val readTopic = sys.env.get("READ_TOPIC").getOrElse("comments-stream")
  val writeTopic = sys.env.get("WRITE_TOPIC").getOrElse("keyword-stream22")

  println("KAFKA_BROKER_URL" ,kafkaBrokerURL)
  println("SCHEMA_REGISTRY_URL", schemaRegistryURL)
  println("APPLICATION_ID", applicationId)
  println("READ_TOPIC", readTopic)
  println("WRITE_TOPIC", writeTopic)

  KeywordDetectionService(
    kafkaBrokerURL,
    schemaRegistryURL,
    applicationId,
    readTopic,
    writeTopic
  )
}
