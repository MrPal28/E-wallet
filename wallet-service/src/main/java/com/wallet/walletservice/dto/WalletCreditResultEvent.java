package com.wallet.walletservice.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import com.wallet.walletservice.constants.TransactionStatus;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class WalletCreditResultEvent implements Serializable{
   private String eventId;
    private String referenceId;

    private Long userId;
    private BigDecimal creditedAmount;
    private BigDecimal currentBalance;

    private TransactionStatus status;   // SUCCESS / FAILED
    private String failureReason;

    private Instant occurredAt;
}
