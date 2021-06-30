from confluent_kafka.admin import AdminClient
from ksql import KSQLAPI
import time
import logging
import json
import pprint

required_topic = 'keyword-stream'

logging.basicConfig(level=logging.INFO)
admin_client = AdminClient({'bootstrap.servers': 'localhost:29092'})
topics = admin_client.list_topics().topics
client = KSQLAPI('http://localhost:8088')

# check kafka topics
while required_topic not in topics:
    logging.info("Topic doesn't exist")
    time.sleep(3)
    topics = admin_client.list_topics().topics

# create ksql stream
try:
    client.ksql("CREATE STREAM IF NOT EXISTS keywordsSelectorNew (keywords ARRAY<STRING>) WITH (KAFKA_TOPIC='keyword-stream', VALUE_FORMAT='avro')")
except:
    logging.info("Stream was already created")

# create explode ksql stream
try:
    client.ksql("CREATE STREAM IF NOT EXISTS exploded_keywords AS SELECT EXPLODE(keywords) AS keyword FROM keywordsSelectorNew")
except:
    logging.info("Stream was already created")

test_query = client.query("SELECT keyword, COUNT(1) AS qty FROM exploded_keywords GROUP BY keyword EMIT CHANGES LIMIT 9459")
result = dict()

try:
    for row in test_query:
        row_value = json.loads(row)
        if row_value is not None and 'header' not in row_value:
            key, value = row_value['row']['columns']
            result[key] = value
except:
    logging.info("All the records were consumed")
    pass
finally:
    test_query.close()

sorted_result = sorted(result.items(), key=lambda x: x[1], reverse=True)

pprint.pprint(sorted_result[0:10])