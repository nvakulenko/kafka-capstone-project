
import io.confluent.ksql.api.client.BatchedQueryResult;
import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.ClientOptions;
import io.confluent.ksql.api.client.ExecuteStatementResult;
import io.confluent.ksql.api.client.Row;
import io.confluent.ksql.api.client.StreamInfo;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentimentsReporterApplication {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(SentimentsReporterApplication.class);

  private static final String LANG_TOPIC_NAME = "lang-stream";
  private static final String SENTIMENT_TOPIC_NAME = "sentiment-out";

  public static void main(final String[] args) {
    ClientOptions options = ClientOptions.create()
        .setHost("ksql-server")
        .setPort(8088);
    Client client = Client.create(options);

    try {
      List<StreamInfo> streams = client.listStreams().get(4L, TimeUnit.SECONDS);

      for (StreamInfo stream : streams) {
        System.out.println(
            stream.getName()
                + " " + stream.getTopic()
                + " " + stream.getKeyFormat()
                + " " + stream.getValueFormat()
                + " " + stream.isWindowed()
        );
      }
      // Send requests with the client by following the other examples

      String sql = "CREATE STREAM sentiments_stream6 (sentiment VARCHAR) "
          + "WITH (kafka_topic='sentiment-out', value_format='delimited');";
      ExecuteStatementResult executeStatementResult = client.executeStatement(sql).get();

      BatchedQueryResult batchedQueryResult = client.executeQuery(
          "SELECT sentiment, count(1) AS sentimentCount "
              + "FROM sentiments_stream6 "
              + "GROUP BY sentiment;");

      // Wait for query result
      List<Row> resultRows = batchedQueryResult.get();

      System.out.println("Received results. Num rows: " + resultRows.size());
      for (Row row : resultRows) {
        System.out.println("Row: " + row.values());
      }

      // cleanup drop streams
      client.executeQuery("DROP STREAM sentiments_stream6;").get();
    } catch (Exception e) {
      LOGGER.error(e.getLocalizedMessage(), e);
    } finally {
      // Terminate any open connections and close the client
      client.close();
    }
  }

}