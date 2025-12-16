package com.wallet.transactionservice.Entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.wallet.transactionservice.constants.TransactionStatus;
import com.wallet.transactionservice.constants.TransactionType;

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
@Table(name = "transactions",
       indexes = {
           @Index(name = "idx_ref_id", columnList = "referenceId"),
           @Index(name = "idx_user", columnList = "fromUserId")
       })
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fromUserId;
    private Long toUserId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private BigDecimal amount;

    @Column(unique = true, nullable = false)
    private String referenceId;

    private String failureReason;

    private Instant createdAt;
    private Instant updatedAt;
}
