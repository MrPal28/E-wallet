package com.wallet.transactionservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransactionRequest {
    private Long toUserId;     // receiver
    private BigDecimal amount;
    private String type;       // TRANSFER, TOPUP, WITHDRAWAL
}
