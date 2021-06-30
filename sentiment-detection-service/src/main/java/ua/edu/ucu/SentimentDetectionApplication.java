package ua.edu.ucu;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.core.credential.AzureKeyCredential;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentimentDetectionApplication {

    private static final Logger logger = LoggerFactory
        .getLogger(SentimentDetectionApplication.class.getName());

    public static void main(String args[]) {
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("48bccf3b94f945d2a837ed994adebe30"))
            .endpoint("https://solomiya-text-api.cognitiveservices.azure.com/")
            .buildClient();

        Properties props_consumer = new Properties();
        props_consumer.put("bootstrap.servers", "broker:9092");
        props_consumer.put("group.id", "sentiments");
        props_consumer.put("key.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer");
        props_consumer.put("value.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer");
        props_consumer.put("ConsumerConfig.AUTO_OFFSET_RESET_CONFIG","earliest");
        props_consumer.put("auto.offset.reset", "earliest");

        Properties props_producer = new Properties();
        props_producer.put("bootstrap.servers", "broker:9092");
        props_producer.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props_producer.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        // === create topic ===
        Admin admin = Admin.create(props_producer);
        NewTopic newTopic = new NewTopic("sentiment-out", 1, (short)1); //new NewTopic(topicName, numPartitions, replicationFactor)

        List<NewTopic> newTopics = new ArrayList<NewTopic>();
        newTopics.add(newTopic);

        admin.createTopics(newTopics);
        admin.close();


        KafkaConsumer<String, String> consumer =
            new KafkaConsumer<String, String>(props_consumer);

        consumer.subscribe(Collections.singletonList("comments-stream"));

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props_producer);

        Duration timeout = Duration.ofMillis(100);

    while (true) {
        try {
            ConsumerRecords<String, String> records = consumer.poll(timeout);
            for (ConsumerRecord<String, String> record : records) {

                DocumentSentiment documentSentiment = textAnalyticsClient
                    .analyzeSentiment(record.value());

                logger.info("Key: " + record.key() + ", Value:" + record.value());
                logger.info("Partition:" + record.partition() + ",Offset:" + record.offset());
                logger.info("\nAnalyzed document sentiment: %s.%n",
                    documentSentiment.getSentiment());

                ProducerRecord<String, String> new_record =
                    new ProducerRecord<>("sentiment-out", record.key(),
                        documentSentiment.getSentiment().toString());
                try {
                    logger.info("Send: " + new_record);

                    producer.send(new_record);
                    producer.flush();

                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
                logger.info("done");
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
        // producer.close();
    }
}
