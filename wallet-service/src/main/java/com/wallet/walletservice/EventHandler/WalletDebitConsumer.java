package com.wallet.walletservice.EventHandler;

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

    @KafkaListener(
            topics = IN_TOPIC,
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "4"
    )
    public void onDebit(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            WalletDebitRequest request =
                    mapper.readValue(record.value(), WalletDebitRequest.class);

            log.info(
                    "→ Debit Request txnId={} user={} amount={}",
                    request.getReferenceId(),
                    request.getUserId(),
                    request.getAmount()
            );

            // BUSINESS LOGIC
            walletService.debit(
                    request.getUserId(),
                    request.getAmount(),
                    request.getReferenceId(),
                    request.getReferenceType()
            );

            // SUCCESS PATH
            ack.acknowledge();
            log.info("✔ Debit processed txnId={} user={}",
                    request.getReferenceId(),
                    request.getUserId());

        } catch (Exception ex) {
            // ONLY system failures reach here
            log.error(
                    "✖ System failure while processing debit event payload={}",
                    record.value(),
                    ex
            );

            kafkaEventPublisher.sendToDLQ(record.value());
            ack.acknowledge(); // avoid poison pill loop
        }
    }
}
