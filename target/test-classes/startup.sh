#!/bin/bash

docker run -d  -p 9092:9092 -e ADV_HOST=127.0.0.1 landoop/fast-data-dev:latest

#create topic source and destination
kafka-topics --bootstrap-server localhost:9092 --topic balance-bank --create
kafka-topics --bootstrap-server localhost:9092 --topic bank-transaction --create


# launch a Kafka consumer
kafka-console-consumer --bootstrap-server localhost:9092 \
    --topic balance-bank \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.DoubleDeserializer

# launch the streams application

# then produce data to it
kafka-console-producer --broker-list localhost:9092 --topic bank-transaction

# package your application as a fat jar
mvn clean package

# run your fat jar
java -jar <your jar here>.jar

# list all topics that we have in Kafka (so we can observe the internal topics)
bin/kafka-topics.sh --list --zookeeper localhost:2181



#create topic with log compaction

kafka-topics --bootstrap-server localhost:9092 --topic fav-color --create --config cleanup.policy=compact --config delete.retention.ms=100 --config segment.ms=100 --config min.cleanable.dirty.ratio=0.01

{"name":"john","amount":"100","time":"2014-12-12T10:39:40Z"}
{"name":"john","amount":"200","time":"2015-12-12T10:39:40Z"}
{"name":"john","amount":"300","time":"2013-12-12T10:39:40Z"}
{"name":"luiz","amount":"400","time":"2015-12-12T10:39:40Z"}
{"name":"luiz","amount":"500","time":"2016-12-12T10:39:40Z"}
{"name":"john","amount":"600","time":"2012-12-12T10:39:40Z"}