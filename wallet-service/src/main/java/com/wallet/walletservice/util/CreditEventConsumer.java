package com.example.demo.util;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.demo.dto.TransferEvent;
import com.example.demo.entity.Wallet;
import com.example.demo.repo.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreditEventConsumer {

    private final WalletRepository walletRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String LISTENING_TOPIC = "wallet-credit-events";
    private static final String PRODUCING_TOPIC = "transaction-topic";

    /**
     * Listener for Credit Events.
     * Triggers after a successful DEBIT and credits the receiver wallet.
     */
    @KafkaListener(topics = LISTENING_TOPIC, groupId = "wallet-service")
    public void consumeCreditEvent(ConsumerRecord<String, String> record) {
        System.out.println("=== [CreditEventConsumer] Received from " + LISTENING_TOPIC + " ===");
        System.out.println("Key: " + record.key());
        System.out.println("Message: " + record.value());

        try {
            TransferEvent event = objectMapper.readValue(record.value(), TransferEvent.class);
            processCredit(event);
        } catch (Exception e) {
            System.err.println("[CreditEventConsumer] Failed to deserialize or process message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processCredit(TransferEvent event) {
        System.out.println("[CreditEventConsumer] Processing credit for TxId: " + event.getTransactionId());
        try {
            Wallet receiver = walletRepository.findByUserId(event.getToUserId())
                    .orElseThrow(() -> new RuntimeException("Receiver wallet not found: " + event.getToUserId()));

            // Credit wallet
            receiver.setWalletBalance(receiver.getWalletBalance().add(event.getAmount()));
            walletRepository.save(receiver);
            System.out.println("[CreditEventConsumer] Credit successful for userId: " + event.getToUserId());

            // Update status to COMPLETED
            event.setStatus("COMPLETED");
            sendEvent(event.getToUserId(), event);

        } catch (Exception ex) {
            System.err.println("[CreditEventConsumer] Exception: " + ex.getMessage());
            ex.printStackTrace();
            event.setStatus("FAILED");
            sendEvent(event.getFromUserId(), event);
        }
    }

    private void sendEvent(Long keyUserId, TransferEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(PRODUCING_TOPIC, keyUserId.toString(), payload);
            System.out.println("[CreditEventConsumer] Published event to " + PRODUCING_TOPIC + ": " + payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}