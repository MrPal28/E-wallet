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
public class DebitEventConsumer {

    private final WalletRepository walletRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String LISTENING_TOPIC = "wallet-debit-events";
    private static final String PRODUCING_TOPIC = "wallet-credit-events";

    /**
     * Listener for Debit Events.
     * Triggers when a transaction INITIATES debit from the sender wallet.
     */
    @KafkaListener(topics = LISTENING_TOPIC, groupId = "wallet-service")
    public void consumeDebitEvent(ConsumerRecord<String, String> record) {
        System.out.println("=== [DebitEventConsumer] Received from " + LISTENING_TOPIC + " ===");
        System.out.println("Key: " + record.key());
        System.out.println("Message: " + record.value());

        try {
            TransferEvent event = objectMapper.readValue(record.value(), TransferEvent.class);
            processDebit(event);
        } catch (Exception e) {
            System.err.println("[DebitEventConsumer] Failed to deserialize or process message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processDebit(TransferEvent event) {
        System.out.println("[DebitEventConsumer] Processing debit for TxId: " + event.getTransactionId());
        try {
            Wallet sender = walletRepository.findByUserId(event.getFromUserId())
                    .orElseThrow(() -> new RuntimeException("Sender wallet not found: " + event.getFromUserId()));

            if (sender.getWalletBalance().compareTo(event.getAmount()) < 0) {
                System.out.println("[DebitEventConsumer] Insufficient funds for userId: " + event.getFromUserId());
                event.setStatus("FAILED");
                sendEvent(event.getFromUserId(), event);
                return;
            }

            // Debit wallet
            sender.setWalletBalance(sender.getWalletBalance().subtract(event.getAmount()));
            walletRepository.save(sender);
            System.out.println("[DebitEventConsumer] Debit successful for userId: " + event.getFromUserId());

            // Update event status for credit step
            event.setStatus("DEBIT_SUCCESS");
            sendEvent(event.getToUserId(), event);

        } catch (Exception ex) {
            System.err.println("[DebitEventConsumer] Exception: " + ex.getMessage());
            ex.printStackTrace();
            event.setStatus("FAILED");
            sendEvent(event.getFromUserId(), event);
        }
    }

    private void sendEvent(Long keyUserId, TransferEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(PRODUCING_TOPIC, keyUserId.toString(), payload);
            System.out.println("[DebitEventConsumer] Published event to " + PRODUCING_TOPIC + ": " + payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}