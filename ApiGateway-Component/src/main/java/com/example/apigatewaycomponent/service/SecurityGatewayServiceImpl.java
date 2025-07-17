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
    private static final String COMPLETED_EXPECTED_FUTURE_LOG = "Completing expected future response with: {}";
    private static final String REMOVED_EXPECTED_FUTURE_LOG = "Future expectation with correlation id: {} was removed from expectations";
    private static final long REQUEST_TIMEOUT = 5;
    private final KafkaTemplate<String, AuthRequestDTO> securityKafkaTemplate;
    private final Map<String, CompletableFuture<ResponseEntity<Object>>> responseFutures = new ConcurrentHashMap<>();

    public SecurityGatewayServiceImpl(KafkaTemplate<String, AuthRequestDTO> securityKafkaTemplate) {
        this.securityKafkaTemplate = securityKafkaTemplate;
    }

    @Override
    @KafkaListener(topics = "security-error", groupId = "api-gateway",
            containerFactory = "errorDTOKafkaListenerFactory")
    public void handleSecurityErrors(ErrorDTO securityErrorDTO,
                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.error("Received error topic: security-error with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureErrorResponse = responseFutures.get(correlationId);
        LOGGER.info("Complete CompletableFuture exceptionally with message: {} ", securityErrorDTO);
        futureErrorResponse.completeExceptionally(new ResponseStatusException(HttpStatus.valueOf(
                securityErrorDTO.status()), securityErrorDTO.message()));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> authenticateUser(AuthRequestDTO authRequestDTO) {
        String correlationId = getCorrelationId();
        LOGGER.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: user-authentication with correlation id: {} ", correlationId);
        ProducerRecord<String, AuthRequestDTO> topic = new ProducerRecord<>("user-authentication", authRequestDTO);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        securityKafkaTemplate.send(topic);
        LOGGER.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return awaitResponseOrTimeout(futureResponse, correlationId);
    }

    @Override
    @KafkaListener(topics = "user-authentication-response", groupId = "api-gateway",
            containerFactory = "securityDTOKafkaListenerFactory")
    public void handleUserAuthenticationResponse(AuthResponseDTO authResponseDTO,
                                                 @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: user-authentication-response with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.get(correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, authResponseDTO);
        futureResponse.complete(ResponseEntity.ok(authResponseDTO));
    }

    private CompletableFuture<ResponseEntity<Object>> awaitResponseOrTimeout(
            CompletableFuture<ResponseEntity<Object>> futureResponse, String correlationId) {
        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .whenComplete((response, throwable) -> {
                    LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
                    responseFutures.remove(correlationId);
                })
                .thenApply(response -> {
                    if (response != null && futureResponse.isDone()) {
                        LOGGER.info("Request successfully collapsed and received to the Controller");
                        return ResponseEntity.ok(response.getBody());
                    } else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT,
                                "Request timed out, service unreachable, please try again later");
                    }
                });
    }

    private static String getCorrelationId() {
        return UUID.randomUUID().toString();
    }
}