package ua.edu.ucu

object Main extends App {
//  createTopic()

//  KeywordDetectionService(
//    "localhost:29092",
//    "http://localhost:8081",
//    "keyword-detection-service",
//    "write-topic"
//  )

  println(RAKE.run("It's pretty well known and it was a paid product placement. Hamilton advertised the watch around the movie and had an Interstellar page on their website. The biggest product placement was the Carhartt jacket though.   Brands send old merch all the time by the way for movies. Just watch Stranger Things for tons of examples of that. Eggos are prominently featured as Eleven's favorite food"))
}
