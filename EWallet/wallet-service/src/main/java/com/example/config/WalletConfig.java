package com.example.config;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.common.serialization.StringDeserializer;


@Configuration
public class WalletConfig {
	
	@Bean
	ObjectMapper getMapper() {
		return new ObjectMapper();
	}
	
	Properties getProperties() {
		Properties properties = new Properties();
		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		
		/**
		 * Kafka stores messages in key-value pair.the key and value
		 * both are in binary and kafka needs to deserialize them after
		 * receiving from kafka cluster.
		 * 
		 * Here, key will be resembles to partition.
		 * and value to the actual message.
		 */
		
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		return properties;
	}
	
	@Bean
	ConsumerFactory<String, String> getConsumerFactory(){
        return new DefaultKafkaConsumerFactory(getProperties());
    }
	
}
