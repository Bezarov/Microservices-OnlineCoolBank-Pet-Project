package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.AuthRequestDTO;
import com.example.apigatewaycomponent.dto.AuthResponseDTO;
import com.example.apigatewaycomponent.dto.ErrorDTO;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public void handleSecurityErrors(ErrorDTO securityErrorDTO, String correlationId) {
        logger.error("Received error topic with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureErrorResponse = responseFutures.remove(correlationId);
        if (futureErrorResponse != null) {
            logger.info("Complete CompletableFuture exceptionally with message: {} ", securityErrorDTO.toString());
            futureErrorResponse.completeExceptionally(new ResponseStatusException(HttpStatus.valueOf(
                    securityErrorDTO.getStatus()), securityErrorDTO.getMessage()));
        } else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> authenticateUser(AuthRequestDTO authRequestDTO) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("user-authentication", authRequestDTO);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        securityKafkaTemplate.send(topic);

        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null)
                        return ResponseEntity.ok(response.getBody());
                    else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
                    }
                })
                .exceptionally(error -> {
                    if (error.getCause() instanceof ResponseStatusException)
                        throw (ResponseStatusException) error.getCause();
                    else {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                    }
                });
    }

    @Override
    public void handleUserAuthenticationResponse(AuthResponseDTO authResponseDTO,
                                                 @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
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
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("component-authentication", authRequestDTO);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        securityKafkaTemplate.send(topic);

        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null)
                        return ResponseEntity.ok(response.getBody());
                    else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
                    }
                })
                .exceptionally(error -> {
                    if (error.getCause() instanceof ResponseStatusException)
                        throw (ResponseStatusException) error.getCause();
                    else {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                    }
                });
    }

    @Override
    public void handleComponentAuthenticationResponse(AuthResponseDTO authResponseDTO,
                                                      @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(authResponseDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }
}
