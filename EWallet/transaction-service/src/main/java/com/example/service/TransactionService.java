package com.example.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
import com.example.PaymentPurpose;
import com.example.TransactionStatus;
import com.example.entities.Transaction;
import com.example.repository.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TransactionService implements UserDetailsService{
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	TransactionRepository transactionRepository;
	
	@Autowired
	KafkaTemplate<String, String> kafkaTemplate;
	
	@Autowired
	ObjectMapper objectMapper;
	
	private static Logger logger = LoggerFactory.getLogger(TransactionService.class);
	
	
	/**
	 * 
	 * @param username
	 * This method getUserFromUserService is used to getting details from user_service.
	 * First it authenticated himself with provided credentials.
	 * @return
	 */
	private JSONObject getUserFromUserService(String username){

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth("txn_service","txn123");
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
	
	public String initiateTransaction(String sender, String receiver, Double amount, PaymentPurpose paymentPurpose) throws JsonProcessingException {
		
		logger.info("initiateTransaction check1");
		
		Transaction transaction = Transaction.builder()
									.sender(sender)
									.receiver(receiver)
									.amount(amount)
									.paymentPurpose(paymentPurpose)
									.transactionId(UUID.randomUUID().toString())
									.transactionStatus(TransactionStatus.PENDING)
									.build();
		
		transactionRepository.save(transaction);
		logger.info("transaction: "+transaction+" saved successfully in db");
		
		//publish this transaction for consuming by wallet_service to do transaction.
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(CommonConstants.TRANSACTION_CREATION_TOPIC_SENDER, sender);
		jsonObject.put(CommonConstants.TRANSACTION_CREATION_TOPIC_RECEIVER, receiver);
		jsonObject.put(CommonConstants.TRANSACTION_CREATION_TOPIC_AMOUNT, amount);
		jsonObject.put(CommonConstants.TRANSACTION_CREATION_TOPIC_PURPOSE, paymentPurpose);
		jsonObject.put(CommonConstants.TRANSACTION_CREATION_TOPIC_TRANSACTION_ID, transaction.getTransactionId());
		
		kafkaTemplate.send(CommonConstants.TRANSACTION_CREATE_TOPIC, 
				objectMapper.writeValueAsString(jsonObject));
		
		logger.info("send jsonObject to kafka: "+jsonObject);
		
		return transaction.getTransactionId();
	}
	
	@KafkaListener(topics = CommonConstants.WALLET_UPDATED_TOPIC, groupId = "transaction_wallet_update")
	public String updateTransactionFromWallet(String message) throws ParseException {
		
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(message);
		logger.info("jsonObject: "+jsonObject);
		
		TransactionStatus updatedtransactionStatus = TransactionStatus.valueOf((String) jsonObject.get(CommonConstants.WALLET_UPDATE_STATUS));
		String transactionId = (String) jsonObject.get(CommonConstants.TRANSACTION_CREATION_TOPIC_TRANSACTION_ID);
		
		logger.info("updatedtransactionStatus: "+updatedtransactionStatus+" transactionId: "+transactionId);
		
		if(updatedtransactionStatus == TransactionStatus.FAILURE) {
			// send notification to sender about this.
			transactionRepository.updateTransaction(transactionId, TransactionStatus.FAILURE);
		}else {
			// send notification to sender and receiver about this.
			transactionRepository.updateTransaction(transactionId, TransactionStatus.SUCCESS);
		}
		
		logger.info("Given transactionId: "+transactionId+" updated successfully");
		
		return null;
	}
	
}
