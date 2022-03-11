package com.github;

import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.internals.KeyValueIteratorFacade;
import org.slf4j.LoggerFactory;

public class FavoriteColor {
	private static final org.slf4j.Logger log  = LoggerFactory.getLogger(FavoriteColor.class);
	public static void main(String[] args) {

		Properties p = new Properties();
		p.put(StreamsConfig.APPLICATION_ID_CONFIG, "favorite-color-app");
		p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		StreamsBuilder streamsBuilder = new StreamsBuilder();
		KStream<String, String> kStream = streamsBuilder.stream("per-color");
		// Stream from kafka
		// filter get only green, red and blue => [a,green][b,green][c,red][d,blue]
		// map ==> [green,a][green,b][red,c][clue,d]
		// group by key
		// count
		KTable<String, String> kTable = kStream
				.filter((user, color) -> List.of("green","blue","red").contains(color))
			//	.map((key, value) -> new KeyValue<String, String>(value, key))
				.toTable(Named.as("color-count-v2"));
		kTable.groupBy((user,color)-> new KeyValue<>(color,color))
			.count()
			.toStream()
			.to("fav-color",Produced.with(Serdes.String(),Serdes.Long()));
		
		//kTable.groupBy( (key,value) -> value);
		KafkaStreams kStreams = new KafkaStreams(streamsBuilder.build(), p);
		kStreams.start();
		// print the topology
		System.out.println("Topology : " + kStreams.toString());
		Runtime.getRuntime().addShutdownHook(new Thread(()->{
			System.out.println("Kafka stream is about to close .......");
			kStreams.close();
		}));

	}
}
