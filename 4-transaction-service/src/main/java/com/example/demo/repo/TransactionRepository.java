package com.example.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // List<Transaction> findByFromUserIdOrToUserId(Long fromUserId, Long toUserId);
    List<Transaction> findByFromUserIdAndType(Long fromUserId, String type);
    List<Transaction> findByToUserIdAndType(Long toUserId, String type);
}



