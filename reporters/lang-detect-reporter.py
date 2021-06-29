from confluent_kafka.admin import AdminClient
from ksql import KSQLAPI
import time
import logging
import json
import pprint

required_topic = 'lang-stream'

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
    client.create_stream(table_name='languagesStream',
                         columns_type=['subreddit VARCHAR', 'body VARCHAR', 'controversiality VARCHAR', 'score VARCHAR', 'lang VARCHAR'],
                         topic=required_topic,
                         value_format='avro')
except:
    logging.info("Stream was already created")

test_query = client.query("SELECT lang, COUNT(*) AS qty FROM languagesStream GROUP BY lang LIMIT 5000")
result = dict()

try:
    for row in test_query:
        row_value = json.loads(row)['row']
        if row_value is not None:
            key, value = row_value['columns']
            result[key] = value
except:
    logging.info("All the records were consumed")
    pass
finally:
    test_query.close()

pprint.pprint(result)