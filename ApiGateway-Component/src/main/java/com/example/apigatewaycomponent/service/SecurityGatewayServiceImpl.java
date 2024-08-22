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
    private static final Logger logger = LoggerFactory.getLogger(SecurityGatewayServiceImpl.class);

    private static final long REQUEST_TIMEOUT = 5;
    private final KafkaTemplate<String, Object> securityKafkaTemplate;
    private final Map<String, CompletableFuture<ResponseEntity<Object>>> responseFutures = new ConcurrentHashMap<>();

    public SecurityGatewayServiceImpl(KafkaTemplate<String, Object> securityKafkaTemplate) {
        this.securityKafkaTemplate = securityKafkaTemplate;
    }

    @Override
    @KafkaListener(topics = "security-error", groupId = "api-gateway",
            containerFactory = "errorDTOKafkaListenerFactory")
    public void handleSecurityErrors(ErrorDTO securityErrorDTO, String correlationId) {
        logger.error("Received error topic: security-error with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureErrorResponse = responseFutures.remove(correlationId);
        logger.info("Complete CompletableFuture exceptionally with message: {} ", securityErrorDTO.toString());
        futureErrorResponse.completeExceptionally(new ResponseStatusException(HttpStatus.valueOf(
                securityErrorDTO.getStatus()), securityErrorDTO.getMessage()));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> authenticateUser(AuthRequestDTO authRequestDTO) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: user-authentication with correlation id: {} ", correlationId);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("user-authentication", authRequestDTO);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        securityKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);

        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null) {
                        logger.info("Request successfully collapsed and received to the Controller");
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
        logger.info("Response from topic: user-authentication with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(authResponseDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> authenticateComponent(AuthRequestDTO authRequestDTO) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: component-authentication with correlation id: {} ", correlationId);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("component-authentication", authRequestDTO);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        securityKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);

        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null) {
                        logger.info("Request successfully collapsed and received to the Controller");
                        return ResponseEntity.ok(response.getBody());
                    } else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT,
                                "Request timed out, service unreachable, please try again later");
                    }
                });
    }

    @Override
    @KafkaListener(topics = "component-authentication-response", groupId = "api-gateway",
            containerFactory = "securityDTOKafkaListenerFactory")
    public void handleComponentAuthenticationResponse(AuthResponseDTO authResponseDTO,
                                                      @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: delete-user-by-full-name with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", authResponseDTO);
        futureResponse.complete(ResponseEntity.ok(authResponseDTO));

    }
}
