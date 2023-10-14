package com.example.entities;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import com.example.PaymentPurpose;
import com.example.enums.TransactionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Transaction {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@NotBlank
	private String sender;
	
	@NotBlank
	private String receiver;
	
	@NotBlank
	private PaymentPurpose paymentPurpose;
	
	@Enumerated(EnumType.STRING)
	private TransactionStatus transactionStatus;
	
	@Positive
	private Double amount;
}
