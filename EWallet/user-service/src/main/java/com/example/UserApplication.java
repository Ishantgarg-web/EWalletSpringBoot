package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.entities.User;
import com.example.repository.UserRepository;

@SpringBootApplication
public class UserApplication implements CommandLineRunner {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class,args);
    }

    public void run(String... args) throws Exception {
//        User txnServiceUser = User.builder().phoneNo("txn_service")
//                .password(passwordEncoder.encode("txn123"))
//                .authorities(UserConstants.SERVICE_AUTHORITY)
//                .email("txn@gmail.com")
//                .userIdentifier(UserIdentifier.SERVICE_ID)
//                .userIdentifierValue("txn123")
//                .build();
//        userRepository.save(txnServiceUser);
    }
}