package com.wallet.transactionservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.wallet.transactionservice.Entity.Transaction;
import com.wallet.transactionservice.constants.TransactionStatus;
import com.wallet.transactionservice.constants.TransactionType;

import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class TransactionResponse {

    private Long transactionId;
    private String referenceId;

    private Long fromUserId;
    private Long toUserId;

    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    /* Static mapper */
    public static TransactionResponse from(Transaction tx) {
        return TransactionResponse.builder()
                .transactionId(tx.getId())
                .referenceId(tx.getReferenceId())
                .fromUserId(tx.getFromUserId())
                .toUserId(tx.getToUserId())
                .amount(tx.getAmount())
                .type(tx.getType())
                .status(tx.getStatus())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .build();
    }
}
