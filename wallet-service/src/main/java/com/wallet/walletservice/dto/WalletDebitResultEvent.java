package com.wallet.walletservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import com.wallet.walletservice.constants.TransactionStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDebitResultEvent implements Serializable {

    private String eventId;          // unique event id (UUID)
    private String referenceId;      // your current correlation key

    private Long userId;
    private BigDecimal debitedAmount;
    private BigDecimal remainingBalance;
    private TransactionStatus status;   // SUCCESS / FAILED
    private String failureReason; // null if success

    private Instant occurredAt;
    private int version;             // event versioning (critical)
}
