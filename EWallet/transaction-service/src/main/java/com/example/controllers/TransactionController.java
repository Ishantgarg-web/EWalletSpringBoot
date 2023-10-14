package com.example.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PaymentPurpose;
import com.example.service.TransactionService;

@RestController
public class TransactionController {
	
	@Autowired
	TransactionService transactionService;
	
	@PostMapping("/transact")
	private String initiateTransaction(@RequestParam("receiver") String receiver,
									   @RequestParam("amount") Double amount,
									   @RequestParam("paymentPurpose") PaymentPurpose paymentPurpose) {
		return transactionService.initiateTransaction(receiver, amount, paymentPurpose);
	}
	
	
}
