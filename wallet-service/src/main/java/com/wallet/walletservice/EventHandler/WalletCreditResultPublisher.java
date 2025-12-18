package com.wallet.walletservice.EventHandler;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.walletservice.constants.TransactionStatus;
import com.wallet.walletservice.dto.WalletCreditResultEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletCreditResultPublisher {

    private static final String TOPIC = "wallet.credit.result";

    private final Optional<KafkaTemplate<String, String>> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(
            Long userId,
            BigDecimal creditedAmount,
            BigDecimal currentBalance,
            String referenceId,
            TransactionStatus status,
            String failureReason
    ) {
        if (kafkaTemplate.isEmpty()) {
            log.debug("Kafka disabled, skipping credit result publish ref={}", referenceId);
            return;
        }

        try {
            WalletCreditResultEvent event = WalletCreditResultEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .referenceId(referenceId)
                    .userId(userId)
                    .creditedAmount(creditedAmount)
                    .currentBalance(currentBalance)
                    .status(status)
                    .failureReason(failureReason)
                    .occurredAt(Instant.now())
                    .build();

            String payload = objectMapper.writeValueAsString(event);

            kafkaTemplate.get().send(TOPIC, referenceId, payload);

            log.info(
                "Published CREDIT result ref={} status={} balance={}",
                referenceId, status, currentBalance
            );

        } catch (Exception ex) {
            log.error("Failed to publish CREDIT result ref={}", referenceId, ex);
            throw new RuntimeException(ex);
        }
    }
}
