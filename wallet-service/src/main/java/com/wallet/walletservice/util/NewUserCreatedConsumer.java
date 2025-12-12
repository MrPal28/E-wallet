package com.wallet.walletservice.util;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.walletservice.dto.UserCreatedEvent;
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
public class NewUserCreatedConsumer {

    private final WalletService walletService;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "user.created", groupId = "wallet-service")
    public void listen(ConsumerRecord<String, String> rec, Acknowledgment ack) {
        try {
            UserCreatedEvent event = mapper.readValue(rec.value(), UserCreatedEvent.class);
            log.info("Creating Wallet for new userId={}", event.getId());

            walletService.registerNewWallet(event.getId());

            ack.acknowledge();

        } catch (Exception e) {
            log.error("Wallet creation failed — payload={} err={}", rec.value(), e.getMessage());
            ack.acknowledge(); // else replays indefinitely
        }
    }
}
