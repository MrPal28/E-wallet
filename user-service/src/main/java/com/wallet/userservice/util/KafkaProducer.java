package com.wallet.userservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.userservice.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${USER_SERVICE_KAFKA_TOPIC}")
    private String userEventTopic;

    public void sendUserEvent(String userEmail, UserResponse payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);

            log.info("Producing event to Kafka topic='{}', key='{}'", userEventTopic, userEmail);

            kafkaTemplate.send(userEventTopic, userEmail, json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish user event to Kafka: {}", ex.getMessage(), ex);
                        } else {
                            log.debug("Kafka publish success: partition={}, offset={}",
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });

        } catch (Exception ex) {
            log.error("Error serializing Kafka payload: {}", ex.getMessage(), ex);
        }
    }
}
