package com.example.securitycomponent.service;

import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.dto.AuthResponseDTO;
import com.example.securitycomponent.exception.CustomKafkaException;
import com.example.securitycomponent.utils.JwtUtil;
import feign.FeignException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class KafkaAuthServiceImpl implements KafkaAuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAuthServiceImpl.class);
    private final AuthDetailsService authDetailsService;
    private final JwtUtil jwtUtil;
    private final KafkaTemplate<String, AuthResponseDTO> responseDTOKafkaTemplate;

    public KafkaAuthServiceImpl(AuthDetailsService authDetailsService, JwtUtil jwtUtil, KafkaTemplate<String, AuthResponseDTO> responseDTOKafkaTemplate) {
        this.authDetailsService = authDetailsService;
        this.jwtUtil = jwtUtil;
        this.responseDTOKafkaTemplate = responseDTOKafkaTemplate;
    }


    @Override
    @KafkaListener(topics = "user-authentication", groupId = "security-component",
            containerFactory = "usersAuthRequestDTOKafkaListenerFactory")
    public void authenticateUser(AuthRequestDTO authRequestDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: user-authentication with correlation id: {} ", correlationId);
        try {
            LOGGER.info("Authenticating user with email: {}", authRequestDTO.principal());
            authDetailsService.authenticateUser(authRequestDTO);
            LOGGER.info("Authentication successfully for user with email: {}", authRequestDTO.principal());
            LOGGER.info("Trying to generate user token for credentials: {}", authRequestDTO);
            String jwtToken = jwtUtil.generateUserToken(authRequestDTO.principal().toString());
            LOGGER.info("Generated JWT Token: {}", jwtToken);
            LOGGER.info("Trying to create topic: user-authentication-response with correlation id: {} ", correlationId);
            ProducerRecord<String, AuthResponseDTO> responseTopic = new ProducerRecord<>(
                    "user-authentication-response", null, new AuthResponseDTO(jwtToken));
            responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
            responseDTOKafkaTemplate.send(responseTopic);
            LOGGER.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
        } catch (AuthenticationException | FeignException error) {
            LOGGER.error("Authentication failed for User with Email: {} Password: {}",
                    authRequestDTO.principal(), authRequestDTO.credentials());
            throw new CustomKafkaException(HttpStatus.UNAUTHORIZED, String.format(
                            """
                            "Authentication failed": "Invalid email or password" correlationId:%s"
                            """, correlationId));
        }
    }
}