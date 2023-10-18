package com.example.controllers;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dtos.UserCreateRequest;
import com.example.entities.User;
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
	
	// Read user details api.
	// Only authenticated user can access this api.
	@GetMapping("/getUser")
	public ResponseEntity getUserDetailsForUser(@RequestParam("username") String userName) {
		return userService.getUser(userName);
	}
	
	
	// Update user Details api.
	@PutMapping("/updateUser")
	public ResponseEntity<Object> updateUserNameAndEmail(@RequestParam("name") String name,
													@RequestParam("email") String email){
		/**
		 * AUTHENTICATED
		 * 
		 * User can update:
		 * 1. Name
		 * 2. email
		 * 
		 * for password: we need to create other api.
		 */
		return userService.updateUserNameAndEmail(name, email);
	}
	
	
	// this api will be called to get Authenicated user details.
	@GetMapping("/user")
	public User getUserDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) authentication.getPrincipal();
		return userService.loadUserByUsername(user.getUsername());
	}
	
	@GetMapping("/admin/user/{phoneNo}")
	public User getUserDetails(@PathVariable("phoneNo") String phoneNo) {
		return userService.loadUserByUsername(phoneNo);
	}
	
}
