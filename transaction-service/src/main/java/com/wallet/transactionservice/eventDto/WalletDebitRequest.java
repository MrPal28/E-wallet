package com.wallet.transactionservice.eventDto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletDebitRequest {

    private String referenceId;     // MUST be globally unique → idempotent
    private Long userId;              // Whose wallet to debit
    private BigDecimal amount;        // Debit amount
    private String referenceType;     // TXN / ORDER / WITHDRAWAL / P2P
    private Long eventTimestamp;      // Audit + debugging
    private String requestSource;     // "TRANSACTION-SERVICE"
}