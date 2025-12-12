package com.wallet.walletservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.walletservice.dto.WalletDebitRequest;
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
public class WalletDebitConsumer {

    private final ObjectMapper mapper;
    private final WalletService walletService;
    private final KafkaEventPublisher kafkaEventPublisher;

    private static final String IN_TOPIC = "wallet.debit.request";
    private static final String DLQ_TOPIC = "wallet.debit.dlq";

    @KafkaListener(topics = IN_TOPIC, groupId = "wallet-service", concurrency = "4")
    public void onDebit(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            WalletDebitRequest request = mapper.readValue(record.value(), WalletDebitRequest.class);
            log.info("→ Debit Request Received txnId={} amount={} user={} topic={}",
                    request.getTransactionId(), request.getAmount(), request.getUserId(), record.topic());

            walletService.debit(
                    request.getUserId(),
                    request.getAmount(),
                    request.getTransactionId(),
                    request.getReferenceType()
            );

            ack.acknowledge(); // process complete
            log.info("Debit Success txnId={} user={}", request.getTransactionId(), request.getUserId());

        } catch (Exception ex) {
            log.error("Debit Failed txn={}, reason={}", record.key(), ex.getMessage());

            kafkaEventPublisher.sendToDLQ(record.value(), DLQ_TOPIC);
            ack.acknowledge(); // avoid infinite retry loop
        }
    }
}
