package com.example.demo.util;

import java.time.LocalDateTime;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.demo.dto.TransactionEvent;
import com.example.demo.entity.Transaction;
import com.example.demo.repo.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "transaction-status", groupId = "transaction-service")
    public void consumeTransaction(ConsumerRecord<String, String> record) {
        System.out.println("***************New Event Arrived************************");
        try {
            // Deserialize JSON to TransactionEvent
            TransactionEvent event = objectMapper.readValue(record.value(), TransactionEvent.class);

            // Map event to Transaction entity
            Transaction transaction = new Transaction();
            transaction.setTransferId(event.getTransferId());
            transaction.setFromUserId(event.getFromUserId());
            transaction.setToUserId(event.getToUserId());
            transaction.setAmount(event.getAmount());
            transaction.setType(event.getType());
            transaction.setStatus(event.getStatus());
            transaction.setCreatedAt(LocalDateTime.now());
            

            // Persist in DB
            transactionRepository.save(transaction);
            System.out.println("Transaction saved successfully: " + transaction.getTransactionId());

        } catch (JsonProcessingException e) {
            // Log error properly
            System.err.println("Failed to deserialize transaction event: " + e.getMessage());
            // Optional: send to dead-letter topic or alert system
        } catch (Exception e) {
            System.err.println("Error saving transaction: " + e.getMessage());
        }
    }
}
