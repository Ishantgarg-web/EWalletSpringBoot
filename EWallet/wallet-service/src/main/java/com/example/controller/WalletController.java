package com.example.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


import com.example.service.WalletService;


@RestController
public class WalletController {
	
	
	@Autowired
	WalletService walletService;
	
	/**
	 * Need to create an API to check wallet balance. 
	 * This API will be call by user-service with SERVICE_AUTHORITY not by user.
	 */
	
	private static Logger logger = LoggerFactory.getLogger(WalletController.class);
	
	@GetMapping("/wallet/balance")
	public ResponseEntity<Object> getUserWalletBalance(){
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) authentication.getPrincipal();
		logger.info("Logged in username is: "+user.getUsername());
		
		return walletService.getUserBalance(user.getUsername());
	}
	
	
}
