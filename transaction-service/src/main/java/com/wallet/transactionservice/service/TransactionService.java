package com.wallet.transactionservice.service;

import java.math.BigDecimal;

import com.wallet.transactionservice.dto.TransactionResponse;

public interface TransactionService {
  TransactionResponse transfer(Long fromUser, Long toUser, BigDecimal amount);
}
