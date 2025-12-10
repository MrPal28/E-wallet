package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.demo.entity.WalletStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WalletResponse {
    private Long walletId;
    private Long userId;
    private BigDecimal currentBalance;
    private WalletStatus status;
    private Instant updatedAt;
}
