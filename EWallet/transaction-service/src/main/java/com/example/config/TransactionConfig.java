package com.example.config;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class TransactionConfig {
	
	
	@Bean
	ObjectMapper getMapper() {
		return new ObjectMapper();
	}
	
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	@Bean
	PasswordEncoder getPE() {
		return new BCryptPasswordEncoder();
	}
	
	/**
	 * 
	 * Below three methods used for producer properties.
	 */
	
	Properties getProducerProperties() {
		Properties properties = new Properties();
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		
		/**
		 * Kafka stores messages in key-value pair.the key and value
		 * both are in binary and kafka needs to serialize them before
		 * sending to kafka cluster.
		 * 
		 * Here, key will be resembles to partition.
		 * and value to the actual message.
		 */
		
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		return properties;
	}
	
	ProducerFactory getProducerFactory(){
        return new DefaultKafkaProducerFactory(getProducerProperties());
    }

    @Bean
    KafkaTemplate<String,String> getKafkaTemplate(){
        return new KafkaTemplate(getProducerFactory());
    }
	
	
    /**
     * Below two methods used for Consuming kafka properties.
     */
    
    Properties getConsumerProperties() {
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
        return new DefaultKafkaConsumerFactory(getConsumerProperties());
    }
	
    
}
