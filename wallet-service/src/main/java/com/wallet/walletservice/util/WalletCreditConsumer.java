package com.wallet.walletservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.walletservice.dto.WalletCreditRequest;
import com.wallet.walletservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletCreditConsumer {

    private final ObjectMapper mapper;
    private final WalletService walletService;
    private final KafkaEventPublisher kafkaEventPublisher;

    private static final String IN_TOPIC = "wallet.credit.request";
    private static final String DLQ_TOPIC = "wallet.credit.dlq";

    @KafkaListener(topics = IN_TOPIC, groupId = "wallet-service", concurrency = "4")
    public void onCredit(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            WalletCreditRequest request = mapper.readValue(record.value(), WalletCreditRequest.class);
            log.info("→ Credit Request txn={} user={} amount={}", request.getReferenceId(), request.getUserId(), request.getAmount());

            walletService.credit(
                    request.getUserId(),
                    request.getAmount(),
                    request.getReferenceId(),
                    request.getReferenceType()
            );

            ack.acknowledge();
            log.info("Credit Success for txn={} user={}", request.getReferenceId(), request.getUserId());

        } catch (Exception ex) {
            log.error("Credit Failed reason: {}", ex.getMessage());
            kafkaEventPublisher.sendToDLQ(record.value(), DLQ_TOPIC);
            ack.acknowledge();
        }
    }
}
