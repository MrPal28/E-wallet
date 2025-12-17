package com.wallet.transactionservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.wallet.transactionservice.constants.TransactionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class WalletCreditResultEvent {
   private String eventId;
    private String referenceId;

    private Long userId;
    private BigDecimal creditedAmount;
    private BigDecimal currentBalance;

    private TransactionStatus status;   // SUCCESS / FAILED
    private String failureReason;

    private Instant occurredAt;
}
