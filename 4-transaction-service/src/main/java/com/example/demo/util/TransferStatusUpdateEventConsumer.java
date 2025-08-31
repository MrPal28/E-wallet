package com.example.demo.util;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.demo.constants.TransactionStatus;
import com.example.demo.dto.TransferEvent;
import com.example.demo.entity.Transaction;
import com.example.demo.repo.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class TransferStatusUpdateEventConsumer {

    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

        private static final String PRODUCING_TOPIC = "transaction-topic";

    /**
     * Kafka listener for Transaction status updates.
     * Listens to events coming from wallet-service.
     */
    @KafkaListener(topics = PRODUCING_TOPIC, groupId = "transaction-service")
    public void consume(ConsumerRecord<String, String> record) {
        System.out.println("\n====== [Transaction Service] Received Event ======");
        System.out.println("Topic   : " + record.topic());
        System.out.println("Key     : " + record.key());
        System.out.println("Value   : " + record.value());
        System.out.println("===============================================\n");

        try {
            TransferEvent event = objectMapper.readValue(record.value(), TransferEvent.class);
            System.out.println("[Transaction Service] Parsed TransferEvent: " + event);

            // Process event asynchronously (important: don't block Kafka consumer thread)
            CompletableFuture.runAsync(() -> handleWalletEvents(event));

        } catch (JsonProcessingException e) {
            System.err.println("[Transaction Service] ❌ Failed to parse TransferEvent JSON: " + e.getMessage());
        }
    }

    private void handleWalletEvents(TransferEvent event) {
    System.out.println("[Transaction Service] Handling wallet event for TxId=" + event.getTransactionId());

    Optional<Transaction> txOpt = transactionRepository.findById(event.getTransactionId());
    if (txOpt.isEmpty()) {
        System.err.println("[Transaction Service] ❌ No Transaction found for ID: " + event.getTransactionId());
        return;
    }

    Transaction tx = txOpt.get();

    switch (event.getStatus()) {
        case "FAILED":
            System.out.println("[Transaction Service] ❌ Transaction FAILED. Updating status...");
            tx.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(tx);
            break;

        case "DEBIT_SUCCESS":
            System.out.println("[Transaction Service] ✅ Debit completed. Updating DEBIT record...");
            tx.setStatus(TransactionStatus.SUCCESS);
            tx.setType("DEBIT");   // mark this row as debit for sender
            transactionRepository.save(tx);
            break;

        case "COMPLETED":
            System.out.println("[Transaction Service] 🎉 Transaction COMPLETED successfully!");

            // Update debit transaction
            tx.setStatus(TransactionStatus.SUCCESS);
            tx.setType("DEBIT");
            transactionRepository.save(tx);

            // Create separate CREDIT transaction for receiver
            Transaction creditTx = new Transaction();
            creditTx.setFromUserId(event.getFromUserId());
            creditTx.setToUserId(event.getToUserId());
            creditTx.setAmount(event.getAmount());
            creditTx.setStatus(TransactionStatus.SUCCESS);
            creditTx.setType("CREDIT"); // mark as CREDIT for receiver
            transactionRepository.save(creditTx);

            System.out.println("[Transaction Service] 💾 CREDIT transaction created: " + creditTx);
            break;

        default:
            System.err.println("[Transaction Service] ⚠ Unknown event status: " + event.getStatus());
    }

    System.out.println("[Transaction Service] DB updated successfully.");
}

}
