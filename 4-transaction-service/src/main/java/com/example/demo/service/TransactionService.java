package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.AddMoneyRequest;
// import com.example.demo.dto.TransactionRequest;
import com.example.demo.dto.TransactionResponse;
import com.example.demo.dto.TransferRequest;

public interface TransactionService {
    // TransactionResponse createTransaction(Long fromUserId, TransactionRequest request);
    TransactionResponse getTransaction(Long transactionId);
    List<TransactionResponse> getTransactionsByUserId(Long userId);
    TransactionResponse topUp(Long userId, AddMoneyRequest request);
    TransactionResponse transferMoney(Long fromUserId, TransferRequest request);
}
