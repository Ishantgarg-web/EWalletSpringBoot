package com.example.controllers;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.dtos.UserCreateRequest;
import com.example.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
public class UserController {
	
	@Autowired
	UserService userService;
	
	private static Logger logger  = org.slf4j.LoggerFactory.getLogger(UserController.class);
	
	@PostMapping("/user")
	public ResponseEntity createUser(@RequestBody UserCreateRequest userCreateRequest) throws JsonProcessingException{
		userService.create(userCreateRequest);
		return new ResponseEntity<>("user created successfully!", HttpStatus.CREATED);
	}
	
	@GetMapping("/home")
	public ResponseEntity homePage() {
		return new ResponseEntity("This is home page", HttpStatus.OK);
	}
	
}
