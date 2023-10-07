package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entities.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Integer>{
	
}
