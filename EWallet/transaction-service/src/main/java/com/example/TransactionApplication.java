package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.entities.TransactionAuth;
import com.example.repository.TransactionAuthRepository;


@SpringBootApplication
public class TransactionApplication implements CommandLineRunner{
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	TransactionAuthRepository transactionAuthRepository;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SpringApplication.run(TransactionApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
//		TransactionAuth transactionAuth = TransactionAuth.builder()
//											.name("txn_service")
//											.password(passwordEncoder.encode("txn123"))
//											.authority(UserConstants.SERVICE_AUTHORITY)
//											.build();
//		transactionAuthRepository.save(transactionAuth);
	}

}
