package com.example.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.example.PaymentPurpose;
import com.example.TransactionStatus;
import com.example.entities.Transaction;
import com.example.repository.TransactionAuthRepository;
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
	TransactionAuthRepository transactionAuthRepository;
	
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
	 * 
	 * [IMPORTANT]
	 * How does below two methods are working?
	 * When you will passing username and password in Authorization header, then before going in
	 * the /transact api, first it will get the details from user_service about the current
	 * authenticated user. is it valid or not?
	 * 
	 * if everything seems fine, then it will proceed with /transact api.
	 * 
	 * @return
	 */
	private JSONObject getUserFromUserService(String username){
		logger.info("in getUserFromUserService");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth("txn_service","txn123");
        HttpEntity request = new HttpEntity(httpHeaders);
        return restTemplate.exchange("http://localhost:6003/admin/user/"+username, HttpMethod.GET,
                request,JSONObject.class).getBody();
    }
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		logger.info("coming in transactionService...check3");
		JSONObject requestUser = getUserFromUserService(username);
		logger.info("coming in transactionService...check4");
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
	
	/**
	 * Below method is used as kafka listener for topic USER_DELETE_TOPIC
	 * @throws ParseException 
	 * 
	 */
	@KafkaListener(topics = CommonConstants.USER_DELETE_TOPIC, groupId = "deleteUserGroupTransaction")
	public void deleteTransactionsOfUser(String message) throws ParseException {
		
		logger.info("Coming in transaction Service to delete user");
		
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(message);
		
		String username = (String) jsonObject.get(CommonConstants.USER_DELETE_USERID);
		
		logger.info("Given username to delete sender transactions is: "+username);
		
		/**
		 * Now, delete all transactions where sender=username.
		 */
		transactionRepository.deleteSenderTransactions(username);
	}

	public String getTransactionHistory(String username) throws JsonProcessingException {
		/**
		 * It should be call with service-authority.
		 */
		List<Transaction> allTransactions = transactionRepository.getAllTransactions(username);
		
//		for (Transaction txn : allTransactions) {
//			Date date = txn.getCreatedOn();
//		    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//		    txn.setCreatedOn(dateFormat.format(date));
//		}
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("return_records", allTransactions);
		return objectMapper.writeValueAsString(jsonObject);
		
	}
	
}
