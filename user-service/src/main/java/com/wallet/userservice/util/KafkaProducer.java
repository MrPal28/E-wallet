package com.wallet.userservice.util;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.userservice.dto.UserResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
  private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

  @Async
  public void messageToKafka(String userMail , UserResponse userResponse){
		try{
			String message = objectMapper.writeValueAsString(userResponse);
			System.out.println("Message to Kafka: " + message);
			kafkaTemplate.send("user-event", userMail, message);
			System.out.println("Message sent to Kafka successfully");
		}catch (JsonProcessingException e){
			e.printStackTrace();
		}
	}
}
