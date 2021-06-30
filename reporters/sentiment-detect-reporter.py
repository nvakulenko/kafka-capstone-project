from confluent_kafka.admin import AdminClient
from ksql import KSQLAPI
import time
import logging
import json
import pprint

required_topic = 'sentiment-out'

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
    client.create_stream(table_name='sentimentsStream',
                         columns_type=['sentiment VARCHAR'],
                         topic=required_topic,
                         value_format='DELIMITED')
except:
    logging.info("Stream was already created")

test_query = client.query("SELECT sentiment, COUNT(*) AS qty FROM sentimentsStream GROUP BY sentiment EMIT CHANGES LIMIT 5000")
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

pprint.pprint(result)