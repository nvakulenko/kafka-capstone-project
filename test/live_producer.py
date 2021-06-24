from confluent_kafka import avro
from confluent_kafka.avro import AvroProducer
import pandas as pd
import requests

value_schema_str = """
{
   "namespace": "streaming.reddit",
   "name": "comment",
   "type": "record",
   "fields" : [
     {
       "name" : "title",
       "type" : "string"
     },
     {
      "name": "url",
      "type": "string"
    },
    {
      "name": "score",
      "type": "string"
    },
    {
       "name" : "comments",
       "type" : "string"
     },
     {
      "name": "created_date",
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
    'bootstrap.servers': 'localhost:29092',
    'schema.registry.url': 'http://localhost:8081'
    }, default_key_schema=key_schema, default_value_schema=value_schema)

subreddit = 'python'
limit = 100  # num of records
timeframe = 'month'  # hour, day, week, month, year, all
listing = 'top'  # controversial, best, hot, new, random, rising, top


def get_reddit(subreddit, listing, limit, timeframe):
    try:
        base_url = f'https://www.reddit.com/r/{subreddit}/{listing}.json?limit={limit}&t={timeframe}'
        request = requests.get(base_url, headers={'User-agent': 'yourbot'})
    except:
        print('An Error Occured')
    return request.json()


def get_post_titles(r):
    posts = []
    for post in r['data']['children']:
        x = post['data']['title']
        posts.append(x)
    return posts


def get_results(r):
    myDict = {}
    for post in r['data']['children']:
        myDict[post['data']['title']] = {'url': post['data']['url'], 'score': post['data']['score'],
                                         'comments': post['data']['num_comments'],
                                         'created_date': post['data']['created']}
    df = pd.DataFrame.from_dict(myDict, orient='index')
    return df


r = get_reddit(subreddit, listing, limit, timeframe)
df = get_results(r).reset_index().rename(columns={'index': 'title'})
keys = df.columns


def delivery_report(err, msg):
    if err is not None:
        print('Message delivery failed: {}'.format(err))
    else:
        print('Message delivered to {} [{}]'.format(msg.topic(), msg.partition()))

index = 0

for i in range(df.shape[0]):
    print(f"sending {index}")
    value = dict(zip(keys, df.iloc[i].astype('str').values.tolist()))
    print(value, type(value))
    avroProducer.poll(0)
    avroProducer.produce(topic='comments-stream', value=value, key={"id": index}, callback=delivery_report)
    i += 1

avroProducer.flush()