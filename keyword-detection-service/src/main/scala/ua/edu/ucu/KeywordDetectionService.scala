package ua.edu.ucu

import com.sksamuel.avro4s.{Record, RecordFormat}
import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.scala.kstream.{Consumed, Produced}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import io.confluent.kafka.streams.serdes.avro.{GenericAvroSerde, SpecificAvroSerde}
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericRecord, GenericRecordBuilder}
import org.apache.avro.specific.{SpecificData, SpecificRecord}
import org.apache.kafka.streams.scala._

import java.util.{Collections, Properties}

sealed case class SubredditKey(id: Int)
sealed case class Subreddit(subreddit: String, body: String, controversiality: String, score: String)

sealed case class Key(id: Int)
sealed case class Value(value: String)

object KeywordDetectionService {
  def apply(brokerUrl: String, schemaRegistryUrl: String, readTopic: String, writeToTopic: String): Unit = {
    val props: Properties = {
      val p = new Properties()
      p.put(StreamsConfig.APPLICATION_ID_CONFIG, readTopic)
      p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl)
      p.put("schema.registry.url", schemaRegistryUrl)
      p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, classOf[GenericAvroSerde])
      p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, classOf[GenericAvroSerde])
      p
    }

    val serdeConfig = Collections.singletonMap("schema.registry.url", schemaRegistryUrl)



    val builder: StreamsBuilder = new StreamsBuilder

    implicit val consumed = {
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

    val messages = builder.stream[GenericRecord, GenericRecord]("comments-stream")


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
        println(k)
        println(v)

        val kk = Key(1)
        val a = RecordFormat[Key]

        val vv = Value("ser")
        val va = RecordFormat[Value]
        (a.to(kk).asInstanceOf[GenericRecord], va.to(vv).asInstanceOf[GenericRecord])
      })
      .to("tests")
//      .print(Printed.toSysOut)

    val streams: KafkaStreams = new KafkaStreams(builder.build(), props)
    streams.start()

//    streams.close()
  }
}
