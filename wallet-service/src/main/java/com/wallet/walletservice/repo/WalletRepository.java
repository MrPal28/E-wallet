package com.example.demo.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Wallet;


@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {

  Optional<Wallet> findByUserId(@Param("userId") Long userId);

}
