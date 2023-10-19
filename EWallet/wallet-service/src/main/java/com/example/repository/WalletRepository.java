package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.entities.Wallet;

@Transactional
public interface WalletRepository extends JpaRepository<Wallet, Integer>{
	
	Wallet findByPhone(String username);
	
	@Modifying
	@Query("UPDATE Wallet w SET w.balance = w.balance + :amount WHERE w.phone = :user")
	void updateWallet(@Param("user") String user, @Param("amount") Double amount);
	
	@Modifying
	@Query("delete from Wallet where phone= ?1")
	void deleteWallet(String username);
}
