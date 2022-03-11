package com.github;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import com.github.model.Transaction;
import com.github.serdes.JsonSerde;

public class BankBalance {

	public static final String TOPIC_BALANCE_BANK = "balance-bank";
	public static final String TOPIC_BANK_TRANSACTION = "bank-transaction";

	public static void main(String[] args) {
		Properties p = new Properties();
		p.put(StreamsConfig.APPLICATION_ID_CONFIG, "bank-balance-app");
		p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		p.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);//Excatly once by kafka
		
		//create topology
		BankBalance bankBalance =  new BankBalance();
		Topology topology = bankBalance.createTopology();		
		
		//Start kafkaStream
		KafkaStreams kStreams = new KafkaStreams(topology, p);
		kStreams.start();
		
		//close softly
		Runtime.getRuntime().addShutdownHook(new Thread(()->{
			System.out.println("Kafka stream is about to close .......");
			kStreams.close();
		}));
		
	}
	
	public  Topology createTopology() {
		

		StreamsBuilder streamsBuilder = new StreamsBuilder();
		JsonSerde<Transaction> jsonSerde = new JsonSerde<Transaction>(Transaction.class);
		
		KStream<String, Transaction> kStream = streamsBuilder.stream(TOPIC_BANK_TRANSACTION,Consumed.with(Serdes.String(),jsonSerde));
		//make an agregate adder 
		//put result in KTable key=name, value=(sum,date)
		KTable<String,Double> KtableBalance = kStream.selectKey((key,transaction)-> transaction.getName())
													.groupByKey(Grouped.with(Serdes.String(),jsonSerde))
													.aggregate(()-> 0D,
																(user,newTransaction,sumAmount)-> {
																	System.out.println("aggregate transaction : name "
																							+newTransaction.getName()
																							+" Amount "+newTransaction.getAmount()
																							+" Time  "+newTransaction.getTime());
																	return sumAmount + newTransaction.getAmount();
																},
																Materialized.with(Serdes.String(), Serdes.Double())
																);
		
		KtableBalance.toStream().to(TOPIC_BALANCE_BANK,Produced.with(Serdes.String(),Serdes.Double()));
		
		return streamsBuilder.build();
		
	}
}
