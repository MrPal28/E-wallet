package com.wallet.transactionservice.eventController;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.transactionservice.eventDto.WalletCreditRequest;
import com.wallet.transactionservice.eventDto.WalletDebitRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WalletEventPublisher {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    public void debit(WalletDebitRequest req) {
        send("wallet.debit.request", req);
    }

    public void credit(WalletCreditRequest req) {
        send("wallet.credit.request", req);
    }

    private void send(String topic, Object payload) {
        try {
            kafka.send(topic, mapper.writeValueAsString(payload));
        } catch (Exception e) {
            throw new RuntimeException("Kafka publish failed");
        }
    }
}
