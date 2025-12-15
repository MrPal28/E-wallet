package com.wallet.transactionservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AddMoneyRequest {
    private BigDecimal amount;
}

