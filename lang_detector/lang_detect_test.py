from confluent_kafka import avro
from confluent_kafka.avro import AvroConsumer, AvroProducer
from confluent_kafka.avro.serializer import SerializerError
from langdetect import detect
import requests


c = AvroConsumer({
    'bootstrap.servers': 'broker:9092',
    'group.id': 'groupid',
    'schema.registry.url': 'http://schema-registry:8081'})

c.subscribe(['comments-stream'])


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
     },
     {
       "name": "lang",
       "type": "string"
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

index = 0

while True:
    try:
        msg = c.poll(10)

    except SerializerError as e:
        print("Message deserialization failed for {}: {}".format(msg, e))
        break

    if msg is None:
        continue

    if msg.error():
        print("AvroConsumer error: {}".format(msg.error()))
        continue

    msg_val = msg.value()

    try:
        detect_result = detect(msg_val['body'])
        msg_val['lang'] = detect_result

    except:
        msg_val['lang'] = 'undefined'

    print(msg_val)
    avroProducer.poll(0)
    avroProducer.produce(topic='lang-stream', value=msg_val, key={"id": index})
    index += 1


avroProducer.flush()
c.close()