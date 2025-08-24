package com.example.demo.util;



import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.dto.TransactionEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class TransactionStatusProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;


    public void sendTransactionStatus(TransactionEvent event) {
        try{
			String message = objectMapper.writeValueAsString(event);
			System.out.println("Message to Kafka: " + message);
			kafkaTemplate.send("transaction-status",  event.getTransferId().toString(), message);
			System.out.println("Message sent to Kafka successfully");
		}catch (JsonProcessingException e){
			e.printStackTrace();
		}
    }
}
