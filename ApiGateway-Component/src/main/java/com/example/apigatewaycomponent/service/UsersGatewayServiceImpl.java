package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.controller.UsersGatewayController;
import com.example.apigatewaycomponent.dto.UsersDTO;
import com.example.apigatewaycomponent.errordto.UsersErrorDTO;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class UsersGatewayServiceImpl implements UsersGatewayService {
    private static final Logger logger = LoggerFactory.getLogger(UsersGatewayController.class);
    private static final long REQUEST_TIMEOUT = 5;
    private final KafkaTemplate<String, Object> usersKafkaTemplate;
    private final Map<String, CompletableFuture<ResponseEntity<Object>>> responseFutures = new ConcurrentHashMap<>();

    public UsersGatewayServiceImpl(KafkaTemplate<String, Object> usersKafkaTemplate) {
        this.usersKafkaTemplate = usersKafkaTemplate;
    }

    @Override
    @KafkaListener(topics = "users-error", groupId = "api-gateway",
            containerFactory = "usersErrorDTOKafkaListenerFactory")
    public void handleUsersErrors(UsersErrorDTO usersErrorDTO,
                                  @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureErrorResponse = responseFutures.remove(correlationId);
        if (futureErrorResponse != null) {
            futureErrorResponse.completeExceptionally(new ResponseStatusException(HttpStatus.valueOf(
                    usersErrorDTO.getStatus()), usersErrorDTO.getMessage()));
        } else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> createUser(@RequestBody UsersDTO usersDTO) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("create-user", usersDTO);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        usersKafkaTemplate.send(topic);

        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null)
                        return ResponseEntity.ok(response.getBody());
                    else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
                    }
                })
                .exceptionally(error -> {
                    logger.error("Error occurred while processing request: ", error);
                    if (error.getCause() instanceof ResponseStatusException)
                        throw (ResponseStatusException) error.getCause();
                    else {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                    }
                });
    }

    @Override
    @KafkaListener(topics = "create-user-response", groupId = "api-gateway",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void handleUserCreationResponse(UsersDTO usersDTO,
                                           @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(usersDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");

        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getUserById(String userId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-user-by-id", userId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        usersKafkaTemplate.send(topic);

        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null)
                        return ResponseEntity.ok(response.getBody());
                    else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
                    }
                })
                .exceptionally(error -> {
                    logger.error("Error occurred while processing request: ", error);
                    if (error.getCause() instanceof ResponseStatusException)
                        throw (ResponseStatusException) error.getCause();
                    else {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                    }
                });
    }

    @Override
    @KafkaListener(topics = "get-user-by-id-response", groupId = "api-gateway",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void handleGetUserByIdResponse(UsersDTO usersDTO,
                                          @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(usersDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");

        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getUserByEmail(String userEmail) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-user-by-email", userEmail);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        usersKafkaTemplate.send(topic);

        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null)
                        return ResponseEntity.ok(response.getBody());
                    else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
                    }
                })
                .exceptionally(error -> {
                    logger.error("Error occurred while processing request: ", error);
                    if (error.getCause() instanceof ResponseStatusException)
                        throw (ResponseStatusException) error.getCause();
                    else {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                    }
                });
    }

    @Override
    @KafkaListener(topics = "get-user-by-email-response", groupId = "api-gateway",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void handleGetUserByEmailResponse(UsersDTO usersDTO,
                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(usersDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");

        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getUserByFullName(String userFullName) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-user-by-full-name", userFullName);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        usersKafkaTemplate.send(topic);

        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null)
                        return ResponseEntity.ok(response.getBody());
                    else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
                    }
                })
                .exceptionally(error -> {
                    logger.error("Error occurred while processing request: ", error);
                    if (error.getCause() instanceof ResponseStatusException)
                        throw (ResponseStatusException) error.getCause();
                    else {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                    }
                });
    }

    @Override
    @KafkaListener(topics = "get-user-by-full-name-response", groupId = "api-gateway",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void handleGetUserByFullNameResponse(UsersDTO usersDTO,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(usersDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");

        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getUserByPhoneNumber(String userPhoneNumber) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-user-by-phone-number", userPhoneNumber);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        usersKafkaTemplate.send(topic);

        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null)
                        return ResponseEntity.ok(response.getBody());
                    else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
                    }
                })
                .exceptionally(error -> {
                    logger.error("Error occurred while processing request: ", error);
                    if (error.getCause() instanceof ResponseStatusException)
                        throw (ResponseStatusException) error.getCause();
                    else {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                    }
                });
    }

    @Override
    @KafkaListener(topics = "get-user-by-phone-number-response", groupId = "api-gateway",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void handleGetUserByPhoneNumberResponse(UsersDTO usersDTO,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(usersDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");

        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateUser(String userId, UsersDTO usersDTO) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        Map<String, UsersDTO> updateRequestMap = new HashMap<>();
        updateRequestMap.put(userId, usersDTO);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("update-user-by-id", updateRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        usersKafkaTemplate.send(topic);

        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null)
                        return ResponseEntity.ok(response.getBody());
                    else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
                    }
                })
                .exceptionally(error -> {
                    logger.error("Error occurred while processing request: ", error);
                    if (error.getCause() instanceof ResponseStatusException)
                        throw (ResponseStatusException) error.getCause();
                    else {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                    }
                });
    }

    @Override
    @KafkaListener(topics = "update-user-by-id-response", groupId = "api-gateway",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void handleUpdateUserByIdResponse(UsersDTO usersDTO,
                                                   @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(usersDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");

        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updatePasswordById(String userId, String newPassword) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        Map<String, String> updateUserPasswordRequestMap = new HashMap<>();
        updateUserPasswordRequestMap.put(userId, newPassword);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("update-user-password-by-id",
                updateUserPasswordRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        usersKafkaTemplate.send(topic);

        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null)
                        return ResponseEntity.ok(response.getBody());
                    else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
                    }
                })
                .exceptionally(error -> {
                    logger.error("Error occurred while processing request: ", error);
                    if (error.getCause() instanceof ResponseStatusException)
                        throw (ResponseStatusException) error.getCause();
                    else {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                    }
                });
    }

    @Override
    @KafkaListener(topics = "update-user-password-by-id-response", groupId = "api-gateway",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void handleUpdateUserPassordByIdResponse(UsersDTO usersDTO,
                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(usersDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");

        }
    }

    @Override
    public ResponseEntity<String> deleteUserById(UUID userId) {
        return null;
    }

    @Override
    public ResponseEntity<String> deleteUserByEmail(String userEmail) {
        return null;
    }

    @Override
    public ResponseEntity<String> deleteUserByFullName(String userFullName) {
        return null;
    }
}
