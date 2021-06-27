package tutorial;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.core.credential.AzureKeyCredential;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class consumer1 {
    public static void main(String args[])
    {
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
                .credential(new AzureKeyCredential("..."))
                .endpoint("https://solomiya-text-api.cognitiveservices.azure.com/")
                .buildClient();

        Properties props_consumer = new Properties();
        props_consumer.put("bootstrap.servers", "localhost:29092");
        props_consumer.put("group.id", "MyCounter");
        props_consumer.put("key.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer");
        props_consumer.put("value.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer");
        props_consumer.put("ConsumerConfig.AUTO_OFFSET_RESET_CONFIG","earliest");
        props_consumer.put("auto.offset.reset", "earliest");

        Properties props_producer = new Properties();
        props_producer.put("bootstrap.servers", "localhost:29092");
        props_producer.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props_producer.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");


        KafkaConsumer<String, String> consumer =
            new KafkaConsumer<String, String>(props_consumer);

        consumer.subscribe(Collections.singletonList("test-sentiment"));

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props_producer);


        Duration timeout = Duration.ofMillis(100);
    while (true) {
        ConsumerRecords<String, String> records = consumer.poll(timeout);
        for (ConsumerRecord<String, String> record : records) {

            DocumentSentiment documentSentiment = textAnalyticsClient.analyzeSentiment(record.value());

            System.out.printf(record.value().toString());
            System.out.printf("\nAnalyzed document sentiment: %s.%n", documentSentiment.getSentiment());

            ProducerRecord<String, String> new_record =
                    new ProducerRecord<>("sentiment-out", record.key().toString(), documentSentiment.getSentiment().toString());
            try {
                producer.send(new_record).get();
                producer.flush();
                producer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.printf("done");

        }
    }

    }
}
