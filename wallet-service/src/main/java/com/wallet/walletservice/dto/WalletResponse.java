package com.wallet.walletservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.wallet.walletservice.constants.WalletStatus;

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
