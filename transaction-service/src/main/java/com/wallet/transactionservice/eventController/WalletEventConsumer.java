package com.wallet.transactionservice.eventController;

import java.time.Instant;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.transactionservice.Entity.Transaction;
import com.wallet.transactionservice.constants.TransactionStatus;
import com.wallet.transactionservice.eventDto.WalletCreditRequest;
import com.wallet.transactionservice.eventDto.WalletDebitSuccessEvent;
import com.wallet.transactionservice.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WalletEventConsumer {

  private final TransactionRepository repo;
  private final WalletEventPublisher publisher;
  private final ObjectMapper mapper;

  @KafkaListener(topics = "wallet-debit-success", groupId = "${spring.kafka.consumer.group-id}")
  public void onSuccessEvent(String msg) throws Exception {
    WalletDebitSuccessEvent e = mapper.readValue(msg, WalletDebitSuccessEvent.class);

    Transaction tx = repo.findByReferenceId(e.getReferenceId())
        .orElseThrow();

    publisher.credit(
        WalletCreditRequest.builder()
            .userId(tx.getToUserId())
            .amount(tx.getAmount())
            .referenceId(tx.getReferenceId())
            .referenceType("TRANSACTION")
            .build());
  }

  @KafkaListener(topics = "wallet.credit.success" , groupId = "${spring.kafka.consumer.group-id}")
  public void onCreditSuccess(String msg) throws Exception {

    WalletDebitSuccessEvent e = mapper.readValue(msg, WalletDebitSuccessEvent.class);

    Transaction tx = repo.findByReferenceId(e.getReferenceId())
        .orElseThrow();

    tx.setStatus(TransactionStatus.SUCCESS);
    tx.setUpdatedAt(Instant.now());
    repo.save(tx);
  }
}
