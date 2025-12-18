package com.wallet.walletservice.EventHandler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private static final String WALLET_DEBIT_DLQ = "wallet.debit.dlq";

    private final KafkaTemplate<String, String> kafka;

    public void sendToDLQ(String payload) {
        kafka.send(WALLET_DEBIT_DLQ, payload);
        log.warn("DLQ-ROUTED | topic={} | payload={}", WALLET_DEBIT_DLQ, payload);
    }
}
