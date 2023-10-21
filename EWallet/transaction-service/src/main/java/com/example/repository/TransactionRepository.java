package com.example.repository;

import java.util.List;

import org.json.simple.JSONObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.example.TransactionStatus;
import com.example.entities.Transaction;

@Transactional
public interface TransactionRepository extends JpaRepository<Transaction, Integer>{
	
	/***
	 * 
	 * @param transactionId
	 * @param transactionStatus
	 * 
	 * 
	 * [IMPORTANT]
	 * When you write any custom query, that is related to modifying like update or delete.
	 * please mention @Modifying annotation with @Transactional annotation.
	 */
	
	
	@Modifying
	@Query("update Transaction t set t.transactionStatus = ?2 where t.transactionId = ?1")
	void updateTransaction(String transactionId, TransactionStatus transactionStatus);
	
	@Modifying
	@Query("delete from Transaction where sender= ?1")
	void deleteSenderTransactions(String username);
	
	@Query(value =  "select * from Transaction where sender = ?1 or receiver = ?1", nativeQuery = true)
	List<Transaction> getAllTransactions(String username);
	
}
