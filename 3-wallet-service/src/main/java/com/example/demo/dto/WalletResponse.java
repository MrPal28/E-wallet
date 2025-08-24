package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.demo.entity.WalletStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WalletResponse {
    private int walletId;
    private Long userId;
    private BigDecimal walletBalance;
    private WalletStatus status;
    private LocalDate lastUpdated;
}
