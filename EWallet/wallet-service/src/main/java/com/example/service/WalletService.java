package com.example.service;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

import com.example.CommonConstants;
import com.example.UserIdentifier;
import com.example.entities.Wallet;
import com.example.repository.WalletRepository;

@Configuration
public class WalletService {
	
	@Autowired
	WalletRepository walletRepository;
	
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(WalletService.class);
	
	/**
	 * need to create a method that will consume message from user-service
	 * and will create a wallet based on details.
	 * @throws ParseException 
	 */
	
	@KafkaListener(topics = CommonConstants.USER_CREATION_TOPIC, groupId = "grp123")
	public void create(String message) throws ParseException {
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(message);
		
		logger.info("check1 in wallet service: "+message);
		
		// Now, extract details from jsonObject into variables.
		int userid = (int) jsonObject.get(CommonConstants.USER_CREATION_TOPIC_USERID);
		String phone = (String) jsonObject.get(CommonConstants.USER_CREATION_TOPIC_PHONE);
		UserIdentifier userIdentifier = (UserIdentifier) jsonObject.get(CommonConstants.USER_CREATION_TOPIC_IDENTIFIER_KEY);
		String userIdentifierValue = (String) jsonObject.get(CommonConstants.USER_CREATION_TOPIC_IDENTIFIER_VALUE);
		
		logger.info("coming here...check1");
		
		Wallet wallet = Wallet.builder()
							.userid(userid)
							.phone(phone)
							.userIdentitifier(userIdentifier)
							.userIdentifierValue(userIdentifierValue)
							.balance(100.0)
							.build();
		logger.info("coming here...check2");
		
		wallet = walletRepository.save(wallet);
		
		logger.info("wallet saved successfully in db");
	}
	
}
