package com.wallet.transactionservice.util;

import java.math.BigDecimal;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.transactionservice.constants.TransactionStatus;
import com.wallet.transactionservice.dto.TransferEvent;
import com.wallet.transactionservice.entity.Transaction;
import com.wallet.transactionservice.repo.TransactionRepository;

import lombok.RequiredArgsConstructor;



@Component
@RequiredArgsConstructor
public class TransferMoneyEventProducer {

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "wallet-debit-events";

    /**
     * Initiates a new transfer between two users.
     * Creates separate DEBIT and CREDIT rows for tracking.
     */
    public Transaction initiateTransfer(Long fromUser, Long toUser, BigDecimal amount) {
        // 1️⃣ Create DEBIT Transaction (sender side)
        Transaction debitTx = new Transaction();
        debitTx.setFromUserId(fromUser);
        debitTx.setToUserId(toUser);
        debitTx.setAmount(amount);
        debitTx.setType("DEBIT");
        debitTx.setStatus(TransactionStatus.PENDING);
        transactionRepository.save(debitTx);

        // // 2️⃣ Create CREDIT Transaction (receiver side)
        // Transaction creditTx = new Transaction();
        // creditTx.setFromUserId(fromUser);
        // creditTx.setToUserId(toUser);
        // creditTx.setAmount(amount);
        // creditTx.setType("CREDIT");
        // creditTx.setStatus(TransactionStatus.PENDING);
        // transactionRepository.save(creditTx);

        // 3️⃣ Build event for Kafka (tie both debit & credit using parentTxId if needed)
        TransferEvent event = new TransferEvent();
        event.setTransactionId(debitTx.getTransactionId()); // reference debit first
        event.setFromUserId(fromUser);
        event.setToUserId(toUser);
        event.setAmount(amount);
        event.setStatus("INITIATED");

        try {
            String jsonEvent = objectMapper.writeValueAsString(event);

            // 4️⃣ Publish INITIATED event to Kafka (partitioned by sender)
            kafkaTemplate.send(TOPIC, fromUser.toString(), jsonEvent);

            System.out.println("Published INITIATED event to Kafka (key=" + fromUser + "): " + jsonEvent);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize TransferEvent", e);
        }

        // return the DEBIT transaction (primary reference)
        return debitTx;
    }
}
