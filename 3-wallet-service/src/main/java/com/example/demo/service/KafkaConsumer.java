package com.example.demo.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.demo.dto.UserResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {

  private final ObjectMapper objectMapper;
  private final WalletService walletService;

  @KafkaListener(topics = "Newuser-topic",groupId = "wallet-service-id")
  public void consume(ConsumerRecord<String, String> record){
  System.out.println("******u have a notification********");
	System.out.println(record.key());
	System.out.println(record.value());
	String jsonTxt=record.value();
	try {
		UserResponse user=objectMapper.readValue(jsonTxt, UserResponse.class);
		System.out.println("User Details: "+user);
    System.out.println("User Email: "+user.getEmail());
    walletService.registerNewWallet(user.getId());
	} catch (JsonProcessingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }

}
