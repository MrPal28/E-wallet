package com.example.demo.service;

import java.util.List;

// import com.example.demo.dto.TransactionRequest;
import com.example.demo.dto.TransactionResponse;

public interface TransactionService {
    // TransactionResponse createTransaction(Long fromUserId, TransactionRequest request);
    TransactionResponse getTransaction(Long transactionId);
    List<TransactionResponse> getTransactionsByUserId(Long userId);
}
