package com.example.service;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
		logger.info("check1");
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
	
	public ResponseEntity<Object> getUser(String userName) {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) authentication.getPrincipal();
		
		if(user != null && user.getUsername().equals(userName)) {
			User tempUser = loadUserByUsername(userName);
			if(tempUser == null) {
				return new ResponseEntity<>("Please provide proper details",HttpStatus.BAD_REQUEST);
			}
			tempUser.setPassword("We can not show");
			return new ResponseEntity<Object>(loadUserByUsername(userName),HttpStatus.OK);
		}
		return new ResponseEntity<>("Please provide proper details",HttpStatus.BAD_REQUEST);
	}

	public ResponseEntity<Object> updateUserNameAndEmail(String name, String email){
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) authentication.getPrincipal();
		logger.info("user1: "+user);
		user = loadUserByUsername(user.getUsername());
		logger.info("user2: "+user);
		/***
		 * Now, we need to update name and email of this authenticate user.
		 */
		user.setName(name);
		user.setEmail(email);
		
		user = userRepository.save(user);
		
		return new ResponseEntity<Object>(user,HttpStatus.ACCEPTED);
		
	}

	public ResponseEntity<Object> updateUserPassword(String currentPassword, String newPassword,
			String confirmPassword) {
		
		/**
		 * It is authenticated.
		 */
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		
		User user = loadUserByUsername(username);
		
		logger.info("user: "+user);
		/**
		 * First encode the current password, to check with user stored password.
		 */
		String encodeCurrentPassword = passwordEncoder.encode(currentPassword);
		logger.info("user: "+user);
		
		if(passwordEncoder.matches(currentPassword, user.getPassword())) {
			/**
			 * Check, newPassword is same as confirmPassword.
			 */
			if(newPassword.equals(confirmPassword)) {
				/**
				 * Need to update new password in db, but in encoded form.
				 */
				String encodeNewPassword = passwordEncoder.encode(newPassword);
				user.setPassword(encodeNewPassword);
				userRepository.save(user);
				return new ResponseEntity<Object>("Password updated Successfully!!",
						HttpStatus.ACCEPTED);
			}
			return new ResponseEntity<Object>("New Password not match with Confirm Password",
					HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<Object>("Please provide correct current password", 
				HttpStatus.BAD_REQUEST);
	}

	/**
	 * This is working as kafka producer. is request goes well, then we can return successfull
	 * response to user and redirect the user to home page.
	 * @return
	 */
	public ResponseEntity<Object> deleteUser() {
		
		/**
		 * First get Authenticated, user Details
		 */
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = loadUserByUsername(authentication.getName());
		
		/**
		 * Now, we need to delete this user.
		 * First, request goes to wallet for delete wallet.
		 * Second, request goes to transaction for delete all associated
		 *  sender transactions to current user.
		 * 
		 */
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(CommonConstants.USER_DELETE_USERID, user.getUsername());
		
		try {
			kafkaTemplate.send(CommonConstants.USER_DELETE_TOPIC,
					objectMapper.writeValueAsString(jsonObject));
		}catch(Exception e) {
			logger.info("There is an error in sending kafka message for user deletion to"
					+ "wallet and transaction services.");
			return new ResponseEntity<Object>("There is error in deleting user", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		/**
		 * After delete wallet and transaction for given user.
		 * Now, need to delete user from user-service.
		 */
		
		try {
			userRepository.delete(user);
		}catch (Exception e) {
			return new ResponseEntity<Object>("Wallet and Transactions if there"
					+ " are deleted successfully. But there is issue in delete user.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<Object>("User with given username: "+user.getUsername()+""
				+ " deleted successfully", HttpStatus.OK);
	}

}
