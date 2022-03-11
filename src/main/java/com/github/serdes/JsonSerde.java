package com.github.serdes;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


public class JsonSerde<T> implements Serde<T> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
    	JavaTimeModule module = new JavaTimeModule();
    	OBJECT_MAPPER.registerModule(module);
    }

    private Class<T> type;

    public JsonSerde(Class<T> type){
        this.type=type;
    }

    @Override
    public Serializer<T> serializer() {
        return (topic,data)->serialize(data);
    }

    private byte[] serialize(T data){
        try {
			return OBJECT_MAPPER.writeValueAsBytes(data);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }
    private T deserialize(byte[] data){
        try {
			return OBJECT_MAPPER.readValue(data, type);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }

    @Override
    public Deserializer<T> deserializer() {
        return (topic,data)->deserialize(data);
    }
    
}
