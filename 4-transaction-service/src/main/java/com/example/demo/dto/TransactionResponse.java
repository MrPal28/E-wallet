package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionResponse {
    private Long transactionId;
    private Long fromUserId;
    private Long toUserId;
    private BigDecimal amount;
    private String type;
    private String status;
    private LocalDateTime createdAt;
}
