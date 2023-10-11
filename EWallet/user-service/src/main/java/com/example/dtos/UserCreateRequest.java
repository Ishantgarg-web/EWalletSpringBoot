package com.example.dtos;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;

import com.example.UserIdentifier;
import com.example.entities.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {
	
	@NotBlank
	private String name;
	
	@NotBlank
	private String email;
	
	@NotBlank
	private String phone;
	
	
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
				.userIdentifier(this.userIdentifier)
				.userIdentifierValue(this.userIdentifierValue)
				.build();
	}
}
