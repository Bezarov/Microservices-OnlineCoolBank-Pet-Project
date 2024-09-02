package com.example.securitycomponent.service;

import com.example.securitycomponent.dto.AuthRequestDTO;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class KafkaAuthServiceImpl implements KafkaAuthService{
    private static final Logger logger = LoggerFactory.getLogger(KafkaAuthServiceImpl.class);

    @Override
    public void authenticateUser(AuthRequestDTO authRequestDTO, @Header(KafkaHeaders.CORRELATION_ID)  String correlationId) {
        logger.info("Got request from kafka topic: update-user-password-by-id with correlation id: {} ", correlationId);

        logger.info("Trying to create topic: update-user-password-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "update-user-password-by-id-response", null, responseUserDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    public void authenticateToken(String token, @Header(KafkaHeaders.CORRELATION_ID)  String correlationId) {

    }
}
