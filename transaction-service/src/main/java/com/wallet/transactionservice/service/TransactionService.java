package com.wallet.transactionservice.service;

import java.math.BigDecimal;

import com.wallet.transactionservice.Entity.Transaction;

public interface TransactionService {
  Transaction transfer(Long fromUser, Long toUser, BigDecimal amount);
}
