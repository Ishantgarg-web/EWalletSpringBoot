package com.example.securityconfig;

import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;


/***
 * 
 * @author ishant
 * @Configuration is used as when you need to define custom beans 
 * with specific configurations or when you want to manage 
 * non-Spring-managed objects as beans.
 * 
 */

@Configuration
public class UserConfig {
	
	@Bean
	PasswordEncoder getPE() {
		return new BCryptPasswordEncoder();
	}
	
	// Here, we will be writing kafka configuration
	
	@Bean
	ObjectMapper getMapper() {
		return new ObjectMapper();
	}
	
	@Bean
	RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
	
	// Here, we are using Properties not HashMap, because Properties
	// is a thread-safe.
	
	Properties getProperties() {
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
        return new DefaultKafkaProducerFactory(getProperties());
    }

    @Bean
    KafkaTemplate<String,String> getKafkaTemplate(){
        return new KafkaTemplate(getProducerFactory());
    }
	
}
