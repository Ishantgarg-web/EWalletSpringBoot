package com.example.controllers;

import java.util.List;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PaymentPurpose;
import com.example.entities.Transaction;
import com.example.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
public class TransactionController {
	
	@Autowired
	TransactionService transactionService;
	
	private static Logger logger = LoggerFactory.getLogger(TransactionController.class);
	
	@PostMapping("/transact")
	private String initiateTransaction(@RequestParam("receiver") String receiver,
									   @RequestParam("amount") Double amount,
									   @RequestParam("paymentPurpose") PaymentPurpose paymentPurpose) throws JsonProcessingException {
	
		// [IMPORTANT]
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) authentication.getPrincipal();
		
		logger.info("transactionService Field passes: "+user.getUsername() +" "+receiver+" "+amount+" "+paymentPurpose);
		return transactionService.initiateTransaction(user.getUsername(),receiver, amount, paymentPurpose);
	}
	
	@GetMapping("/transaction/history/{username}")
	private String getTransactionHistory(@PathVariable("username") String username) throws JsonProcessingException{
		logger.info("Coming in TransactionController: "+username);
		return transactionService.getTransactionHistory(username);
	}
	
	
}
