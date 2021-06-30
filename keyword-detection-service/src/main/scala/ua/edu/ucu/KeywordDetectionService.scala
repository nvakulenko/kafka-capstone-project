package ua.edu.ucu

import com.sksamuel.avro4s.RecordFormat
import org.apache.kafka.streams.scala.kstream.{Consumed, Produced}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.streams.scala._
import java.util.{Collections, Properties}

sealed case class SubredditKey(id: Int)
sealed case class Subreddit(subreddit: String, body: String, controversiality: String, score: String)

sealed case class KeywordsKey(id: Int)
sealed case class KeywordsValue(keywords: List[String])

object KeywordDetectionService {
  def apply(brokerUrl: String, schemaRegistryUrl: String, applicationId: String, readTopic: String, writeToTopic: String): Unit = {
    val props: Properties = {
      val p = new Properties()
      p.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId)
      p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl)
      p.put("schema.registry.url", schemaRegistryUrl)
      p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, classOf[GenericAvroSerde])
      p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, classOf[GenericAvroSerde])
      p
    }

    val serdeConfig = Collections.singletonMap("schema.registry.url", schemaRegistryUrl)
    implicit val consumed: Consumed[GenericRecord, GenericRecord] = {
      val keyGenericAvroSerde = {
        val s = new GenericAvroSerde
        s.configure(serdeConfig, true) // `true` for record keys
        s
      }

      val valueGenericAvroSerde =  {
        val s = new GenericAvroSerde
        s.configure(serdeConfig, false)
        s
      }

      Consumed.`with`(keyGenericAvroSerde, valueGenericAvroSerde)
    }

    implicit val produced = {
      val keyGenericAvroSerde = {
        val s = new GenericAvroSerde
        s.configure(serdeConfig, true) // `true` for record keys
        s
      }

      val valueGenericAvroSerde =  {
        val s = new GenericAvroSerde
        s.configure(serdeConfig, false)
        s
      }

      Produced.`with`(keyGenericAvroSerde, valueGenericAvroSerde)
    }

    val builder: StreamsBuilder = new StreamsBuilder
    val messages = builder.stream[GenericRecord, GenericRecord](readTopic)

    messages
      .map((k, v) => {
        val key = SubredditKey(k.get("id").toString.toInt)
        val value = Subreddit(
          v.get("subreddit").toString,
          v.get("body").toString,
          v.get("controversiality").toString,
          v.get("score").toString
        )
        (key, value)
      })
      .map((k, v) => {
        val results = RAKE.run(v.body)

        println("received", k, v)

        (KeywordsKey(k.id), KeywordsValue(results.slice(0, 2).map(_._1)))
      })
      .map((keywordsKey, keywordsValue) => {
        val recordsKey = RecordFormat[KeywordsKey]
        val recordsValue = RecordFormat[KeywordsValue]

        println("send",
          recordsKey.to(keywordsKey).asInstanceOf[GenericRecord],
          recordsValue.to(keywordsValue).asInstanceOf[GenericRecord]
        )

        (
          recordsKey.to(keywordsKey).asInstanceOf[GenericRecord],
          recordsValue.to(keywordsValue).asInstanceOf[GenericRecord]
        )
      })
      .to(writeToTopic)

    val streams: KafkaStreams = new KafkaStreams(builder.build(), props)
    streams.start()
  }
}
