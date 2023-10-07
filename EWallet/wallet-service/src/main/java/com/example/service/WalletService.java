package com.example.service;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.example.CommonConstants;
import com.example.UserIdentifier;
import com.example.entities.Wallet;
import com.example.repository.WalletRepository;

@Configuration
public class WalletService {
	
	@Autowired
	WalletRepository walletRepository;
	
	/**
	 * need to create a method that will consume message from user-service
	 * and will create a wallet based on details.
	 * @throws ParseException 
	 */
	
	public void create(String message) throws ParseException {
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(message);
		
		// Now, extract details from jsonObject into variables.
		int userid = (int) jsonObject.get(CommonConstants.USER_CREATION_TOPIC_USERID);
		String phone = (String) jsonObject.get(CommonConstants.USER_CREATION_TOPIC_PHONE);
		UserIdentifier userIdentifier = (UserIdentifier) jsonObject.get(CommonConstants.USER_CREATION_TOPIC_IDENTIFIER_KEY);
		String userIdentifierValue = (String) jsonObject.get(CommonConstants.USER_CREATION_TOPIC_IDENTIFIER_VALUE);
		
		Wallet wallet = Wallet.builder()
							.userid(userid)
							.phone(phone)
							.userIdentitifier(userIdentifier)
							.userIdentifierValue(userIdentifierValue)
							.balance(100.0)
							.build();
		wallet = walletRepository.save(wallet);
		
	}
	
}
