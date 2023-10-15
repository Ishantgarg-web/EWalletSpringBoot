package com.example.service;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.CommonConstants;
import com.example.UserConstants;
import com.example.dtos.UserCreateRequest;
import com.example.entities.User;
import com.example.repository.UserRepository;
import com.example.securityconfig.UserConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class UserService implements UserDetailsService{
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	KafkaTemplate<String, String> kafkaTemplate;
	
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(UserService.class);
	
	@Override
	public User loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByPhoneNo(username);
	}
	
	public void create(UserCreateRequest userCreateRequest) throws JsonProcessingException {
		User user = userCreateRequest.toUser();
		user.setAuthorities(UserConstants.USER_AUTHORITITY);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user = userRepository.save(user);
		logger.info("user created successfully");
		// Now, we need to publish this message to kafka, and this will
		// be consume by other consumers.
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(CommonConstants.USER_CREATION_TOPIC_USERID, user.getId());
		jsonObject.put(CommonConstants.USER_CREATION_TOPIC_PHONE, user.getPhoneNo());
		jsonObject.put(CommonConstants.USER_CREATION_TOPIC_IDENTIFIER_KEY, user.getUserIdentifier());
		jsonObject.put(CommonConstants.USER_CREATION_TOPIC_IDENTIFIER_VALUE, user.getUserIdentifierValue());
		
		logger.info(objectMapper.writeValueAsString(jsonObject));
		
		try {
			kafkaTemplate.send(CommonConstants.USER_CREATION_TOPIC, objectMapper.writeValueAsString(jsonObject));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			logger.info("There is error in sending message from user-service in kafka: ");
		}
		
	}
	
}
