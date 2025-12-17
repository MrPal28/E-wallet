package com.wallet.walletservice.EventHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.wallet.walletservice.constants.TransactionStatus;
import com.wallet.walletservice.dto.WalletDebitResultEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletDebitResultPublisher {

    private static final String TOPIC = "wallet.debit.result";

    private final Optional<KafkaTemplate<String, Object>> kafkaTemplate;

    public void publish(
            Long userId,
            BigDecimal debitedAmount,
            BigDecimal remainingBalance,
            String referenceId,
            TransactionStatus status,
            String failureReason
    ) {
        if (kafkaTemplate.isEmpty()) {
            log.debug("Kafka disabled, skipping debit result publish ref={}", referenceId);
            return;
        }

        WalletDebitResultEvent event = WalletDebitResultEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .referenceId(referenceId)
                .userId(userId)
                .debitedAmount(debitedAmount)
                .remainingBalance(remainingBalance)
                .status(status)
                .failureReason(failureReason)
                .occurredAt(Instant.now())
                .build();

        kafkaTemplate.get().send(TOPIC, referenceId, event);

        log.info(
            "Published DEBIT result ref={} status={} remaining={}",
            referenceId, status, remainingBalance
        );
    }
}
