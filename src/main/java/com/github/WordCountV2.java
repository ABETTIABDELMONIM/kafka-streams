package com.github;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;

public class WordCountV2 {
    public static void main(String[] args) {
        Properties p = new Properties();
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-count-app");
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String,String> kStream =  streamsBuilder.stream("sentences")  ;    //Stream from kafka
        //map values to lowercase
        KTable<String,Long> countTable =  kStream.mapValues(value -> value.toLowerCase())
                .flatMapValues(value -> Arrays.asList(value.split(" ")))
                .selectKey((key,value)->value)
                .groupByKey()
                .count(Named.as("count"));
        //flatmap values split by space
        //select key to apply a key we discard the old key
        //group by key
        //count occurences
        countTable.toStream()
                .to("word-count",Produced.with(Serdes.String(), Serdes.Long()));

        KafkaStreams kStreams = new KafkaStreams(streamsBuilder.build(), p);
        kStreams.start();
        //print the topology
        System.out.println("Topology : " + kStreams.toString());
        Runtime.getRuntime().addShutdownHook(new Thread( kStreams::close));

    }
    
}
