package com.example.demo.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransferRequest {
    // private int fromUserId;
    private Long toUserId;
    private BigDecimal amount;
    private String note; // Optional: message shown in app
}
