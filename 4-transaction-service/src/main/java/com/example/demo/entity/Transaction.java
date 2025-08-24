package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "transaction")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "transfer_id")
    private Long transferId;

    @Column(name = "from_user_id")
    private Long fromUserId;   // null if it's a top-up

    @Column(name = "to_user_id")
    private Long toUserId;     // null if it’s withdrawal

    @Column(precision = 38, scale = 2)
    private BigDecimal amount;

    @Column(length = 50)
    private String type;          // DEBIT / CREDIT / TRANSFER / TOPUP / WITHDRAWAL

    @Column(length = 50)
    private String status;        // PENDING / SUCCESS / FAILED

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
