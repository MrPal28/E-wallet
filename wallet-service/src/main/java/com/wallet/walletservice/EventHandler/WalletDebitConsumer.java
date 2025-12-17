package com.wallet.walletservice.EventHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.walletservice.Exceptions.InsufficientBalanceException;
import com.wallet.walletservice.Exceptions.InvalidAmountException;
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

    @KafkaListener(topics = IN_TOPIC, groupId = "${spring.kafka.consumer.group-id}", concurrency = "4")
    public void onDebit(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            WalletDebitRequest request = mapper.readValue(record.value(), WalletDebitRequest.class);
            log.info("→ Debit Request Received txnId={} amount={} user={} topic={}",
                    request.getReferenceId(), request.getAmount(), request.getUserId(), record.topic());

            walletService.debit(
                    request.getUserId(),
                    request.getAmount(),
                    request.getReferenceId(),
                    request.getReferenceType());

            ack.acknowledge(); // process complete
            log.info("Debit Success txnId={} user={}", request.getReferenceId(), request.getUserId());

        } catch (InsufficientBalanceException | InvalidAmountException ex) {
            // business error already published as FAILED
            ack.acknowledge();
        } catch (Exception ex) {
            kafkaEventPublisher.sendToDLQ(record.value(), DLQ_TOPIC);
            ack.acknowledge();
        }

    }
}
