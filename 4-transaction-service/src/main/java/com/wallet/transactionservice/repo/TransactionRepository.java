package com.wallet.transactionservice.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wallet.transactionservice.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // List<Transaction> findByFromUserIdOrToUserId(Long fromUserId, Long toUserId);
    List<Transaction> findByFromUserIdAndType(Long fromUserId, String type);
    List<Transaction> findByToUserIdAndType(Long toUserId, String type);
}



