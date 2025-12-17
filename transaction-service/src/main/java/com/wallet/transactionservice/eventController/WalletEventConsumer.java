package com.wallet.transactionservice.eventController;

import java.time.Instant;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.transactionservice.Entity.Transaction;
import com.wallet.transactionservice.constants.TransactionStatus;
import com.wallet.transactionservice.dto.WalletCreditResultEvent;
import com.wallet.transactionservice.dto.WalletDebitResultEvent;
import com.wallet.transactionservice.eventDto.WalletCreditRequest;
import com.wallet.transactionservice.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WalletEventConsumer {

  private final TransactionRepository repo;
  private final WalletEventPublisher publisher;
  private final ObjectMapper mapper;

  @KafkaListener(topics = "wallet.debit.result", groupId = "${spring.kafka.consumer.group-id}")
public void onDebitResult(String msg) throws Exception {

    WalletDebitResultEvent e =
        mapper.readValue(msg, WalletDebitResultEvent.class);

    Transaction tx = repo.findByReferenceId(e.getReferenceId())
            .orElseThrow();

    // Idempotency
    if (tx.getStatus() == TransactionStatus.SUCCESS ||
        tx.getStatus() == TransactionStatus.FAILED) {
        return;
    }

    //  Debit failed → mark transaction failed
    if (e.getStatus() != TransactionStatus.SUCCESS) {
        tx.setStatus(TransactionStatus.FAILED);
        tx.setUpdatedAt(Instant.now());
        repo.save(tx);
        return;
    }

    //  Debit success → proceed to credit
    publisher.credit(
        WalletCreditRequest.builder()
            .userId(tx.getToUserId())
            .amount(tx.getAmount())
            .referenceId(tx.getReferenceId())
            .referenceType("TRANSACTION")
            .build()
    );

    tx.setStatus(TransactionStatus.PROCESSING);
    repo.save(tx);
}


 @KafkaListener(topics = "wallet.credit.result")
public void onCreditResult(String msg) throws Exception {

    WalletCreditResultEvent e =
        mapper.readValue(msg, WalletCreditResultEvent.class);

    Transaction tx = repo.findByReferenceId(e.getReferenceId())
        .orElseThrow();

    if (tx.getStatus() == TransactionStatus.SUCCESS) return;

    if (e.getStatus() == TransactionStatus.SUCCESS) {
        tx.setStatus(TransactionStatus.SUCCESS);
    } else {
        tx.setStatus(TransactionStatus.FAILED);
    }

    tx.setUpdatedAt(Instant.now());
    repo.save(tx);
}

}
