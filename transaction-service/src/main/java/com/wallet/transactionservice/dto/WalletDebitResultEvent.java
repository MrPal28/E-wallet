package com.wallet.transactionservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.wallet.transactionservice.constants.TransactionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDebitResultEvent {

    private String eventId;          // unique event id (UUID)
    private String referenceId;      // your current correlation key

    private Long userId;
    private BigDecimal debitedAmount;
    private BigDecimal remainingBalance;
    private TransactionStatus status;   // SUCCESS / FAILED
    private String failureReason; // null if success

    private Instant occurredAt;
}
