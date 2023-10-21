package com.example.repository;

import java.util.List;

import org.json.simple.JSONObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;

import com.example.entities.Transaction;
import com.example.entities.TransactionAuth;

public interface TransactionAuthRepository extends JpaRepository<TransactionAuth, Integer>{
	
	@Query(value = "select * from Transaction where sender= ?1", nativeQuery = true)
	ResponseEntity<Object> getAllTransactions(String username);
	
}
