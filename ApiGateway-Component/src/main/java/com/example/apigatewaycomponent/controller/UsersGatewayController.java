package com.example.apigatewaycomponent.controller;

import com.example.apigatewaycomponent.dto.UsersDTO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/users")
public class UsersGatewayController {
    private static final Logger logger = LoggerFactory.getLogger(UsersGatewayController.class);
    private static final long REQUEST_TIMEOUT = 10;
    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, UsersDTO> usersDTOKafkaTemplate;
    private final Map<String, CompletableFuture<UsersDTO>> responseFutures = new ConcurrentHashMap<>();

    public UsersGatewayController(RestTemplate restTemplate, KafkaTemplate<String, UsersDTO> usersDTOKafkaTemplate) {
        this.restTemplate = restTemplate;
        this.usersDTOKafkaTemplate = usersDTOKafkaTemplate;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<UsersDTO>> createUser(@RequestBody UsersDTO usersDTO) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<UsersDTO> future = new CompletableFuture<>();
        responseFutures.put(correlationId, future);

        ProducerRecord<String, UsersDTO> topic = new ProducerRecord<>("user-creation", usersDTO);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        usersDTOKafkaTemplate.send(topic);

        return future.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(ResponseEntity::ok)
                .exceptionally(error -> {
                    logger.error("Error occurred while processing request", error);
                    if (error.getCause() instanceof ResponseStatusException)
                        throw (ResponseStatusException) error.getCause();
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                });
    }

    @KafkaListener(topics = "user-creation-response", groupId = "api-gateway")
    public void handleUserCreationResponse(UsersDTO usersDTO,
                               @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<UsersDTO> future = responseFutures.remove(correlationId);
        if (future != null)
            future.complete(usersDTO);
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");

        }
    }
    @KafkaListener(topics = "user-creation-error", groupId = "api-gateway")
    public void handleError(ConsumerRecord<String, String> record,
                            @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<UsersDTO> future = responseFutures.remove(correlationId);
        if (future != null) {
            future.completeExceptionally(new ResponseStatusException(HttpStatus.BAD_REQUEST, record.value()));
        } else {
            logger.warn("No future found for correlationId: " + correlationId);
        }
    }

    @GetMapping("/getUserById/{userId}")
    public String getUserDetails(@PathVariable String userId) {
        // Получаем данные пользователя из Users-Components
        String userUrl = "http://users-components/user/by-id/" + userId;
        String userResponse = restTemplate.getForObject(userUrl, String.class);

        // Получаем данные аккаунта пользователя из Account-Components
        String accountUrl = "http://account-components/account/by-user-id/" + userId;
        String accountResponse = restTemplate.getForObject(accountUrl, String.class);

        // Получаем данные о картах пользователя из Card-Components
        String cardUrl = "http://card-components/card/by-user-id/" + userId;
        String cardResponse = restTemplate.getForObject(cardUrl, String.class);

        return "User: " + userResponse + "\nAccount: " + accountResponse + "\nCards: " + cardResponse;
    }
}
