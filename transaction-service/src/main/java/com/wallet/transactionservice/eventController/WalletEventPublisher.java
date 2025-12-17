package com.wallet.transactionservice.eventController;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.transactionservice.eventDto.WalletCreditRequest;
import com.wallet.transactionservice.eventDto.WalletDebitRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Component
@RequiredArgsConstructor
@Slf4j
public class WalletEventPublisher {

    private static final String DEBIT_TOPIC = "wallet.debit.request";
    private static final String CREDIT_TOPIC = "wallet.credit.request";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void debit(WalletDebitRequest request) {
        publish(DEBIT_TOPIC, request.getReferenceId(), request);
    }

    public void credit(WalletCreditRequest request) {
        publish(CREDIT_TOPIC, request.getReferenceId(), request);
    }

    private void publish(String topic, String key, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);

            kafkaTemplate.send(topic, key, json);

            log.info(
                "Published event topic={} key={} payload={}",
                topic, key, json
            );

        } catch (Exception ex) {
            log.error(
                "Kafka publish failed topic={} key={} payload={} error={}",
                topic, key, payload, ex.getMessage(), ex
            );

            // In prod: metrics + retry or outbox
            throw new EventPublishException("Failed to publish event to " + topic, ex);
        }
    }

    /* Custom exception for observability */
    public static class EventPublishException extends RuntimeException {
        public EventPublishException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
