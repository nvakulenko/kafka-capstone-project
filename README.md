## Data Streaming course capstone project, summer 2021
Project A. E2E data processing pipeline - processing social media data (reddit)

## Team
Dmytro Voloshyniuk, Solomia Branets, Diana Dmytrashko, Andriy Vedilin, Nataliia Vakulenko

## High component level architecture


## Components

### Kafka cluster
* Zookeeper
* Broker
* Schema-registry
* Ksql-server
* Ksql-cli
* Kafdrop - UI for kafka Broker, hosted on http://localhost:9000
* kafka-create-topics - creates producer topic on initialization of the cluster

### Message producer
Language - Python

Entry point to the application. Reads Reddit comments from prepared .csv file and produces each record as a separate message to kafka topic '' using avro format

### Language detector
Language - Python

Consumer for 'comments-stream' topic. Detect language of the message and Produces message with detected language to 'lang-stream' topic.


### Sentiments detector
Language - Java

Consumer for 'comments-stream' topic. Detects language of a message and produces message with sentiment class to topic 'sentiments-out'. 

Possible sentiment classes: positive, negative, mixed, neutral.

### Keywords detector
Language - Scala
Consumer for 'comments-stream' topic. Extracts keywords of the message and sends them as array to 'keywords-stream' topic.


### Statistics
Language - Python & Ksql

Pythos script with executes ksql queries against ksql-cli server. Reads topics 'lang-stream', 'sentiments-out', 'keywords-stream' 

## Data set
Reddit comments .csv file with 5000 of messages.

## How to run
`docker-compose up --build`

## Results
### List of languages with numbers of messages 

### Number of messages among sentiment classes
### Top 10 keywords
