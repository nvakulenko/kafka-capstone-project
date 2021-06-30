package ua.edu.ucu

object Main extends App {
//  createTopic()

  KeywordDetectionService(
    "localhost:29092",
    "http://localhost:8081",
    "keyword-detection-service",
    "write-topic"
  )
}
