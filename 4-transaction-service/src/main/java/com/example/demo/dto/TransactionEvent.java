package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionEvent {
    private Long transferId;
    private Long fromUserId;
    private Long toUserId;
    private BigDecimal amount;
    private String type;     // DEBIT / CREDIT
    private String status;   // PENDING, SUCCESS, FAILED
    private LocalDateTime createdAt;
}
