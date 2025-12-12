package com.wallet.walletservice.dto;


import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletCreditRequest {

    private String transactionId;     // Reference to original txn (idempotency)
    private Long userId;              // Wallet owner
    private BigDecimal amount;        // Credit amount
    private String referenceType;     // CASHBACK / REFUND / P2P_RECEIVE
    private Long eventTimestamp;      // for trace timeline
    private String requestSource;     // "REWARD-SERVICE" or "TRANSACTION-SERVICE"
}
