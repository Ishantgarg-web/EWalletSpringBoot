package com.example.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.CommonConstants;
import com.example.TransactionStatus;
import com.example.UserIdentifier;
import com.example.entities.Wallet;
import com.example.repository.WalletRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WalletService implements UserDetailsService{
	
	@Autowired
	WalletRepository walletRepository;
	
	@Autowired
	KafkaTemplate<String, String> kafkaTemplate;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	RestTemplate restTemplate;
	
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(WalletService.class);
	
	
	private JSONObject getUserFromUserService(String username){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth("wallet_service","wallet123");
        HttpEntity request = new HttpEntity(httpHeaders);
        return restTemplate.exchange("http://localhost:6003/admin/user/"+username, HttpMethod.GET,
                request,JSONObject.class).getBody();
    }
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		JSONObject requestUser = getUserFromUserService(username);
		// This requestUser is the object that we are getting from user_service.
		
		List<GrantedAuthority> authorities;
        List<LinkedHashMap<String,String>> reqAuthorities = (List<LinkedHashMap<String,String>>)
                requestUser.get("authorities");
        authorities = reqAuthorities.stream().map(x -> x.get("authority"))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        
        // These above make authorities format to return.
        
        return new User((String) requestUser.get("username"),(String)requestUser.get("password")
                ,authorities);
		
	}
	
	
	
	/**
	 * need to create a method that will consume message from user-service
	 * and will create a wallet based on details.
	 * @throws ParseException 
	 */
	
	@KafkaListener(topics = CommonConstants.USER_CREATION_TOPIC, groupId = "grp123")
	public void create(String message) throws ParseException {
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(message);

		logger.info("jsonObject: "+jsonObject);
		
		// Now, extract details from jsonObject into variables.
		Long userid = (Long) jsonObject.get(CommonConstants.USER_CREATION_TOPIC_USERID);
		String phone = (String) jsonObject.get(CommonConstants.USER_CREATION_TOPIC_PHONE);
		String userIdentifier = (String) jsonObject.get(CommonConstants.USER_CREATION_TOPIC_IDENTIFIER_KEY);
		String userIdentifierValue = (String) jsonObject.get(CommonConstants.USER_CREATION_TOPIC_IDENTIFIER_VALUE);

		logger.info("coming here...check1");
		
		Wallet wallet = Wallet.builder()
							.userid(userid)
							.phone(phone)
							.userIdentitifier(UserIdentifier.valueOf(userIdentifier))
							.userIdentifierValue(userIdentifierValue)
							.balance(100.0)
							.build();
		wallet = walletRepository.save(wallet);
		logger.info("wallet saved successfully in db");
	}
	
	/**
	 * Below Listener is used for getting transaction details from transaction_service.
	 * @throws ParseException 
	 * @throws JsonProcessingException 
	 */
	
	@KafkaListener(topics = CommonConstants.TRANSACTION_CREATE_TOPIC, groupId = "transaction_create")
	public void doTransaction(String message) throws ParseException, JsonProcessingException {
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(message);

		logger.info("jsonObject: "+jsonObject);
		
		// Now, extract details from jsonObject into variables.
		String sender = (String) jsonObject.get(CommonConstants.TRANSACTION_CREATION_TOPIC_SENDER);
		String receiver = (String) jsonObject.get(CommonConstants.TRANSACTION_CREATION_TOPIC_RECEIVER);
		Double amount = (Double) jsonObject.get(CommonConstants.TRANSACTION_CREATION_TOPIC_AMOUNT);
		String paymentPurpose = (String) jsonObject.get(CommonConstants.TRANSACTION_CREATION_TOPIC_PURPOSE);
		String transactionId = (String) jsonObject.get(CommonConstants.TRANSACTION_CREATION_TOPIC_TRANSACTION_ID);
		
		// Now, we are getting wallet details of sender and receiver both from walletRepository;
		Wallet senderWallet = walletRepository.findByPhone(sender);
		Wallet receiverWallet = walletRepository.findByPhone(receiver);
		
		/**
		 * Now, following cases can be possible:
		 * 1. if receiverWallet and senderWallet is present or not.
		 * 2. if senderBalance must be less than or equals to amount
		 * 3. if transaction will be success or not.
		 */
		if(senderWallet == null || receiverWallet == null || senderWallet.getBalance()<=amount) {
			logger.info("Failed transaction will proceed here.");
			jsonObject.put(CommonConstants.WALLET_UPDATE_STATUS, TransactionStatus.FAILURE);
		}else if(senderWallet.getBalance()>amount) {
			logger.info("Success transaction will proceed here.");
			jsonObject.put(CommonConstants.WALLET_UPDATE_STATUS, TransactionStatus.SUCCESS);
			
			walletRepository.updateWallet(sender, 0-amount);
			walletRepository.updateWallet(receiver, amount);
			
			// From here, we need to send the success response to notification Service.
		}
		jsonObject.put(CommonConstants.TRANSACTION_CREATION_TOPIC_TRANSACTION_ID, transactionId);
		
		// Basically we send two details transactionId and walletUpdateStatus to listener.
		kafkaTemplate.send(CommonConstants.WALLET_UPDATED_TOPIC,
				objectMapper.writeValueAsString(jsonObject));
		
		logger.info("senderWallet: "+senderWallet);
		logger.info("receiverWallet: "+receiverWallet);
	}
	
	/**
	 * Below method is used as kafka listener for topic USER_DELETE_TOPIC
	 * @throws ParseException 
	 * 
	 */
	@KafkaListener(topics = CommonConstants.USER_DELETE_TOPIC, groupId = "deleteUserGroupWallet")
	public void deleteWalletOfUser(String message) throws ParseException {
		
		logger.info("Coming in wallet Service to delete user");
		
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(message);
		
		String username = (String) jsonObject.get(CommonConstants.USER_DELETE_USERID);
		
		logger.info("Given username to delete wallet is: "+username);
		
		/**
		 * Now, delete wallet where phone=username.
		 */
		walletRepository.deleteWallet(username);
	}

	public ResponseEntity<Object> getUserBalance(String username) {
		
		/**
		 * need to return the balance of the given username.
		 */
		try {
			Wallet wallet = walletRepository.findByPhone(username);
			Double userBalance = wallet.getBalance();
			return new ResponseEntity<Object>("Your Balance is: "+userBalance,
					HttpStatus.OK);
		}catch (Exception e) {
			return new ResponseEntity<Object>("Bank Server Down",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
	
	
}
