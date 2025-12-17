package com.wallet.walletservice.EventHandler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private final KafkaTemplate<String, String> kafka;

    public void sendToDLQ(String topic, String payload) {
        kafka.send(topic, payload);
        log.warn("DLQ-ROUTED | topic={} | payload={}", topic, payload);
    }
}
