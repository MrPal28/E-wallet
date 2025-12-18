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
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletEventConsumer {

    private final TransactionRepository repo;
    private final WalletEventPublisher publisher;
    private final ObjectMapper mapper;

    // ==========================
    // DEBIT RESULT CONSUMER
    // ==========================
    @KafkaListener(
        topics = "wallet.debit.result",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onDebitResult(String msg) {

        log.info("[TXN][DEBIT_RESULT][RECEIVED] payload={}", msg);

        try {
            WalletDebitResultEvent e =
                    mapper.readValue(msg, WalletDebitResultEvent.class);

            log.debug(
                "[TXN][DEBIT_RESULT][PARSED] ref={} status={} user={}",
                e.getReferenceId(), e.getStatus(), e.getUserId()
            );

            Transaction tx = repo.findByReferenceId(e.getReferenceId())
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "Transaction not found ref=" + e.getReferenceId()
                        )
                    );

            log.debug(
                "[TXN][DEBIT_RESULT][TX_STATE] ref={} currentStatus={}",
                tx.getReferenceId(), tx.getStatus()
            );

            // ----------------------------
            // HARD IDEMPOTENCY
            // ----------------------------
            if (tx.getStatus() == TransactionStatus.SUCCESS ||
                tx.getStatus() == TransactionStatus.FAILED) {

                log.info(
                    "[TXN][DEBIT_RESULT][IGNORED] ref={} already terminal status={}",
                    tx.getReferenceId(), tx.getStatus()
                );
                return;
            }

            // ----------------------------
            // DEBIT FAILED
            // ----------------------------
            if (e.getStatus() != TransactionStatus.SUCCESS) {

                log.warn(
                    "[TXN][DEBIT_RESULT][FAILED] ref={} reason={}",
                    e.getReferenceId(), e.getFailureReason()
                );

                tx.setStatus(TransactionStatus.FAILED);
                tx.setFailureReason(e.getFailureReason());
                tx.setUpdatedAt(Instant.now());
                repo.save(tx);

                log.info(
                    "[TXN][DEBIT_RESULT][TX_UPDATED] ref={} status=FAILED",
                    tx.getReferenceId()
                );
                return;
            }

            // ----------------------------
            // DEBIT SUCCESS → TRIGGER CREDIT
            // ONLY WHEN STATUS == INITIATED
            // ----------------------------
            if (tx.getStatus() == TransactionStatus.INITIATED) {

                log.info(
                    "[TXN][DEBIT_RESULT][CREDIT_TRIGGER] ref={} toUser={} amount={}",
                    tx.getReferenceId(),
                    tx.getToUserId(),
                    tx.getAmount()
                );

                publisher.credit(
                        WalletCreditRequest.builder()
                                .userId(tx.getToUserId())
                                .amount(tx.getAmount())
                                .referenceId(tx.getReferenceId())
                                .referenceType("TRANSACTION")
                                .build()
                );

                tx.setStatus(TransactionStatus.PROCESSING);
                tx.setUpdatedAt(Instant.now());
                repo.save(tx);

                log.info(
                    "[TXN][DEBIT_RESULT][TX_UPDATED] ref={} status=PROCESSING",
                    tx.getReferenceId()
                );
            }

        } catch (Exception ex) {
            // swallow to avoid Kafka retry storm
            log.error(
                "[TXN][DEBIT_RESULT][ERROR] payload={}",
                msg,
                ex
            );
        }
    }

    // ==========================
    // CREDIT RESULT CONSUMER
    // ==========================
    @KafkaListener(
        topics = "wallet.credit.result",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onCreditResult(String msg) {

        log.info("[TXN][CREDIT_RESULT][RECEIVED] payload={}", msg);

        try {
            WalletCreditResultEvent e =
                    mapper.readValue(msg, WalletCreditResultEvent.class);

            log.debug(
                "[TXN][CREDIT_RESULT][PARSED] ref={} status={}",
                e.getReferenceId(), e.getStatus()
            );

            Transaction tx = repo.findByReferenceId(e.getReferenceId())
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "Transaction not found ref=" + e.getReferenceId()
                        )
                    );

            log.debug(
                "[TXN][CREDIT_RESULT][TX_STATE] ref={} currentStatus={}",
                tx.getReferenceId(), tx.getStatus()
            );

            // Idempotency
            if (tx.getStatus() == TransactionStatus.SUCCESS ||
                tx.getStatus() == TransactionStatus.FAILED) {

                log.info(
                    "[TXN][CREDIT_RESULT][IGNORED] ref={} already terminal status={}",
                    tx.getReferenceId(), tx.getStatus()
                );
                return;
            }

            tx.setStatus(
                e.getStatus() == TransactionStatus.SUCCESS
                        ? TransactionStatus.SUCCESS
                        : TransactionStatus.FAILED
            );

            tx.setUpdatedAt(Instant.now());
            repo.save(tx);

            log.info(
                "[TXN][CREDIT_RESULT][TX_FINALIZED] ref={} finalStatus={}",
                tx.getReferenceId(), tx.getStatus()
            );

        } catch (Exception ex) {
            log.error(
                "[TXN][CREDIT_RESULT][ERROR] payload={}",
                msg,
                ex
            );
        }
    }
}
