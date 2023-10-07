package com.example.dtos;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;

import com.example.UserIdentifier;
import com.example.entities.User;


public class UserCreateRequest {
	
	@NotBlank
	private String name;
	
	@NotBlank
	private String email;
	
	@NotBlank
	private String phone;
	
	@Enumerated(EnumType.STRING)
	private PaymentPurpose paymentPurpose;
	
	@NotBlank
	private String password;
	
	@NotBlank
	private UserIdentifier userIdentifier;
	
	@NotBlank
	private String userIdentifierValue;
	
	public User toUser() {
		return User.builder()
				.name(this.name)
				.email(this.email)
				.phoneNo(this.phone)
				.password(this.password)
				.paymentPurpose(this.paymentPurpose)
				.userIdentifier(this.userIdentifier)
				.userIdentifierValue(this.userIdentifierValue)
				.build();
	}
}
