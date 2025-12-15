package com.wallet.transactionservice.service;

import java.util.List;

import com.wallet.transactionservice.dto.AddMoneyRequest;
import com.wallet.transactionservice.dto.TransactionResponse;
import com.wallet.transactionservice.dto.TransferRequest;

public interface TransactionService {
    // TransactionResponse createTransaction(Long fromUserId, TransactionRequest request);
    TransactionResponse getTransaction(Long transactionId);
    List<TransactionResponse> getTransactionsByUserId(Long userId);
    TransactionResponse topUp(Long userId, AddMoneyRequest request);
    TransactionResponse transferMoney(Long fromUserId, TransferRequest request);
}
