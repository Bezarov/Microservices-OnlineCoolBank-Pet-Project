package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.AuthRequestDTO;
import com.example.apigatewaycomponent.dto.AuthResponseDTO;
import com.example.apigatewaycomponent.dto.ErrorDTO;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class SecurityGatewayServiceImpl implements SecurityGatewayService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityGatewayServiceImpl.class);
    private static final long REQUEST_TIMEOUT = 5;
    private final KafkaTemplate<String, AuthRequestDTO> securityKafkaTemplate;
    private final KafkaTemplate<String, Map<String, String>> mapStringToStringKafkaTemplate;
    private final Map<String, CompletableFuture<ResponseEntity<Object>>> responseFutures = new ConcurrentHashMap<>();

    public SecurityGatewayServiceImpl(KafkaTemplate<String, AuthRequestDTO> securityKafkaTemplate, KafkaTemplate<String, Map<String, String>> mapStringToStringKafkaTemplate) {
        this.securityKafkaTemplate = securityKafkaTemplate;
        this.mapStringToStringKafkaTemplate = mapStringToStringKafkaTemplate;
    }

    @Override
    @KafkaListener(topics = "security-error", groupId = "api-gateway",
            containerFactory = "errorDTOKafkaListenerFactory")
    public void handleSecurityErrors(ErrorDTO securityErrorDTO,
                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.error("Received error topic: security-error with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureErrorResponse = responseFutures.remove(correlationId);
        LOGGER.info("Complete CompletableFuture exceptionally with message: {} ", securityErrorDTO);
        futureErrorResponse.completeExceptionally(new ResponseStatusException(HttpStatus.valueOf(
                securityErrorDTO.getStatus()), securityErrorDTO.getMessage()));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> authenticateUser(AuthRequestDTO authRequestDTO) {
        String correlationId = UUID.randomUUID().toString();
        LOGGER.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: user-authentication with correlation id: {} ", correlationId);
        ProducerRecord<String, AuthRequestDTO> topic = new ProducerRecord<>("user-authentication", authRequestDTO);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        securityKafkaTemplate.send(topic);
        LOGGER.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null && !futureResponse.isDone()) {
                        LOGGER.info("Request successfully collapsed and received to the Controller");
                        return ResponseEntity.ok(response.getBody());
                    } else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT,
                                "Request timed out, service unreachable, please try again later");
                    }
                });
    }

    @Override
    @KafkaListener(topics = "user-authentication-response", groupId = "api-gateway",
            containerFactory = "securityDTOKafkaListenerFactory")
    public void handleUserAuthenticationResponse(AuthResponseDTO authResponseDTO,
                                                 @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: user-authentication-response with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(authResponseDTO));
        else {
            LOGGER.warn("Response topic with correlationId was not found: {}", correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> authenticateUserToken(String jwtToken, String requestURI) {
        String correlationId = UUID.randomUUID().toString();
        LOGGER.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: user-token-authentication with correlation id: {} ", correlationId);
        Map<String, String> tokenAndURIMap = Map.of(jwtToken, requestURI);
        ProducerRecord<String, Map<String, String>> topic = new ProducerRecord<>("user-token-authentication", tokenAndURIMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        mapStringToStringKafkaTemplate.send(topic);
        LOGGER.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null && !futureResponse.isDone()) {
                        LOGGER.info("Request successfully collapsed and received to JwtAuthenticationFilter");
                        return ResponseEntity.ok(response);
                    } else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT,
                                "Request timed out, service unreachable, please try again later");
                    }
                });
    }

    @Override
    @KafkaListener(topics = "user-token-authentication-response", groupId = "api-gateway",
            containerFactory = "securityContextKafkaListenerFactory")
    public void handleUserTokenAuthenticationResponse(SecurityContextImpl securityContext, String correlationId) {
        LOGGER.info("Response from topic: user-token-authentication-response with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(securityContext));
        else {
            LOGGER.warn("Response topic with correlationId was not found: {}", correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }
}