package com.wallet.walletservice.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.wallet.walletservice.constants.LedgerType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wallet_ledger", indexes = {
        @Index(name = "idx_wallet_id", columnList = "wallet_id"),
        @Index(name = "idx_reference_id", columnList = "referenceId")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class WalletLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String ledgerId;

    @Column(nullable = false)
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerType type;  // CREDIT | DEBIT | REVERSAL | ADJUSTMENT

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    private String referenceId;       // txnId/eventId
    private String referenceType;     // REWARD / PAYMENT / WITHDRAWAL

    @Column(nullable = false)
    @Builder.Default
    private Instant timestamp = Instant.now();

    private String remarks;
}
