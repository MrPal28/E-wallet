package com.wallet.transactionservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransferEvent {
    private Long transactionId;
    // private Long creditTransactionId;
    private Long fromUserId;
    private Long toUserId;
    private BigDecimal amount;
    private String status; // INITIATED, DEBIT_SUCCESS, FAILED, COMPLETED
}

