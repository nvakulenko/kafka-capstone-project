from confluent_kafka import avro
from confluent_kafka.avro import AvroProducer
import csv

value_schema_str = """
{
   "namespace": "streaming.reddit",
   "name": "comment",
   "type": "record",
   "fields" : [
     {
       "name" : "subreddit",
       "type" : "string"
     },
     {
      "name": "body",
      "type": "string"
    },
    {
      "name": "controversiality",
      "type": "string"
    },
    {
       "name" : "score",
       "type" : "string"
     }
   ]
}
"""

key_schema_str = """
{
   "namespace": "streaming.reddit",
   "name": "comment",
   "type": "record",
   "fields" : [
     {
       "name" : "id",
       "type" : "int"
     }
   ]
}
"""

value_schema = avro.loads(value_schema_str)
key_schema = avro.loads(key_schema_str)

avroProducer = AvroProducer({
    'bootstrap.servers': 'broker:9092',
    'schema.registry.url': 'http://schema-registry:8081'
    }, default_key_schema=key_schema, default_value_schema=value_schema)

filepath = 'reddit_dataset.csv'

with open(filepath, encoding="utf8") as file:
    reader = csv.DictReader(file)
    for row in reader:
        avroProducer.poll(0)
        value = row
        key = {"id": reader.line_num}
        avroProducer.produce(topic='kafka-test', value=value, key=key)

avroProducer.flush()