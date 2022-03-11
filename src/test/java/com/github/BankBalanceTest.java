package com.github;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.model.Transaction;
import com.github.serdes.JsonSerde;

@ExtendWith(MockitoExtension.class)
public class BankBalanceTest {
	
    private TopologyTestDriver topologyTestDriver;
    private TestInputTopic<String,Transaction> testInputTopic;
    private TestOutputTopic<String,Double> testOutputTopic;
    @InjectMocks
    private  BankBalance bankBalanceTopology;

    @BeforeEach
    void setUp(){
        //props
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
        
        JsonSerde<Transaction> transactionSerde = new JsonSerde<Transaction>(Transaction.class);
        
        topologyTestDriver = new TopologyTestDriver(bankBalanceTopology.createTopology(), props);
        
        testInputTopic = topologyTestDriver.createInputTopic(
        								BankBalance.TOPIC_BANK_TRANSACTION, 
                                        Serdes.String().serializer(), 
                                        transactionSerde.serializer()); 
        testOutputTopic =  topologyTestDriver.createOutputTopic(
        								BankBalance.TOPIC_BALANCE_BANK, 
                                        Serdes.String().deserializer(),
                                        Serdes.Double().deserializer()
                                        );
    }
    @AfterEach
    void closeAp() {
    	topologyTestDriver.close();
    }
    
    @Test
    @DisplayName("given a transaction t get balance b t.amount == b.value")
    void testScenario1(){
        //create preconditions
        //input
        Transaction transaction = new Transaction().withName("John")
        											.withAmount(100D)
        											.withTime(Instant.now());
        //result
        String user ="John";
        Double balance = 100D;
        //Actions (When)
        testInputTopic.pipeInput(transaction);
        //Verification (Then)
        TestRecord<String, Double> testRecord =  testOutputTopic.readRecord();
        assertEquals(user, testRecord.getKey());
        assertEquals(balance, testRecord.getValue());

    }
    
}
