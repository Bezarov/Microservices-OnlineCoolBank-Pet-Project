package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.ErrorDTO;
import com.example.apigatewaycomponent.dto.UsersDTO;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class UsersGatewayServiceImpl implements UsersGatewayService {
    private static final Logger logger = LoggerFactory.getLogger(UsersGatewayServiceImpl.class);
    private static final long REQUEST_TIMEOUT = 5;
    private final KafkaTemplate<String, UsersDTO> usersDTOKafkaTemplate;
    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private final KafkaTemplate<String, UUID> uuidKafkaTemplate;
    private final KafkaTemplate<String, Map<UUID, UsersDTO>> mapUUIDToDTOKafkaTemplate;
    private final KafkaTemplate<String, Map<UUID, String>> mapUUIDToStringKafkaTemplate;
    private final Map<String, CompletableFuture<ResponseEntity<Object>>> responseFutures = new ConcurrentHashMap<>();

    public UsersGatewayServiceImpl(KafkaTemplate<String, UsersDTO> usersDTOKafkaTemplate, KafkaTemplate<String,
            String> stringKafkaTemplate, KafkaTemplate<String, UUID> uuidKafkaTemplate, KafkaTemplate<String,
            Map<UUID, UsersDTO>> mapUUIDToDTOKafkaTemplate, KafkaTemplate<String,
            Map<UUID, String>> mapUUIDToStringKafkaTemplate) {
        this.usersDTOKafkaTemplate = usersDTOKafkaTemplate;
        this.stringKafkaTemplate = stringKafkaTemplate;
        this.uuidKafkaTemplate = uuidKafkaTemplate;
        this.mapUUIDToDTOKafkaTemplate = mapUUIDToDTOKafkaTemplate;
        this.mapUUIDToStringKafkaTemplate = mapUUIDToStringKafkaTemplate;
    }


    @Override
    @KafkaListener(topics = "users-error", groupId = "api-gateway",
            containerFactory = "errorDTOKafkaListenerFactory")
    public void handleUsersErrors(ErrorDTO usersErrorDTO,
                                  @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.error("Received error topic: users-error with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureErrorResponse = responseFutures.remove(correlationId);
        logger.info("Complete CompletableFuture exceptionally with message: {} ", usersErrorDTO.toString());
        futureErrorResponse.completeExceptionally(new ResponseStatusException(HttpStatus.valueOf(
                usersErrorDTO.getStatus()), usersErrorDTO.getMessage()));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> createUser(@RequestBody UsersDTO usersDTO) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: create-user with correlation id: {} ", correlationId);
        ProducerRecord<String, UsersDTO> topic = new ProducerRecord<>("create-user", usersDTO);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        usersDTOKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "create-user-response", groupId = "api-gateway",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void handleUserCreationResponse(UsersDTO usersDTO,
                                           @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: create-user with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", usersDTO);
        futureResponse.complete(ResponseEntity.ok(usersDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getUserById(UUID userId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        logger.info(SecurityContextHolder.getContext().toString());
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-user-by-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("get-user-by-id", userId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-user-by-id-response", groupId = "api-gateway",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void handleGetUserByIdResponse(UsersDTO usersDTO,
                                          @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-user-by-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", usersDTO);
        futureResponse.complete(ResponseEntity.ok(usersDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getUserByEmail(String userEmail) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-user-by-email with correlation id: {} ", correlationId);
        ProducerRecord<String, String> topic = new ProducerRecord<>("get-user-by-email", userEmail);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        stringKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-user-by-email-response", groupId = "api-gateway",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void handleGetUserByEmailResponse(UsersDTO usersDTO,
                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-user-by-email with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", usersDTO);
        futureResponse.complete(ResponseEntity.ok(usersDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getUserByFullName(String userFullName) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-user-by-full-name with correlation id: {} ", correlationId);
        ProducerRecord<String, String> topic = new ProducerRecord<>("get-user-by-full-name", userFullName);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        stringKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-user-by-full-name-response", groupId = "api-gateway",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void handleGetUserByFullNameResponse(UsersDTO usersDTO,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-user-by-full-name with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", usersDTO);
        futureResponse.complete(ResponseEntity.ok(usersDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getUserByPhoneNumber(String userPhoneNumber) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-user-by-phone-number with correlation id: {} ", correlationId);
        ProducerRecord<String, String> topic = new ProducerRecord<>("get-user-by-phone-number", userPhoneNumber);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        stringKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-user-by-phone-number-response", groupId = "api-gateway",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void handleGetUserByPhoneNumberResponse(UsersDTO usersDTO,
                                                   @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-user-by-phone-number with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", usersDTO);
        futureResponse.complete(ResponseEntity.ok(usersDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateUser(UUID userId, UsersDTO usersDTO) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: update-user-by-id with correlation id: {} ", correlationId);
        Map<UUID, UsersDTO> updateRequestMap = Map.of(userId, usersDTO);
        ProducerRecord<String, Map<UUID, UsersDTO>> topic = new ProducerRecord<>("update-user-by-id", updateRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        mapUUIDToDTOKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "update-user-by-id-response", groupId = "api-gateway",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void handleUpdateUserByIdResponse(UsersDTO usersDTO,
                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: update-user-by-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", usersDTO);
        futureResponse.complete(ResponseEntity.ok(usersDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updatePasswordById(UUID userId, String newPassword) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: update-user-password-by-id with correlation id: {} ", correlationId);
        Map<UUID, String> updateUserPasswordRequestMap = Map.of(userId, newPassword);
        ProducerRecord<String, Map<UUID, String>> topic = new ProducerRecord<>(
                "update-user-password-by-id", updateUserPasswordRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        mapUUIDToStringKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "update-user-password-by-id-response", groupId = "api-gateway",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void handleUpdateUserPasswordByIdResponse(UsersDTO usersDTO,
                                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: update-user-password-by-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", usersDTO);
        futureResponse.complete(ResponseEntity.ok(usersDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteUserById(UUID userId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: delete-user-by-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("delete-user-by-id", userId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "delete-user-by-id-response", groupId = "api-gateway",
            containerFactory = "stringKafkaListenerFactory")
    public void handleDeleteUserByIdResponse(String responseMessage,
                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: delete-user-by-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", responseMessage);
        futureResponse.complete(ResponseEntity.ok(responseMessage));
    }


    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteUserByEmail(String userEmail) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: delete-user-by-email with correlation id: {} ", correlationId);
        ProducerRecord<String, String> topic = new ProducerRecord<>("delete-user-by-email", userEmail);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        stringKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "delete-user-by-email-response", groupId = "api-gateway",
            containerFactory = "stringKafkaListenerFactory")
    public void handleDeleteUserByEmailResponse(String responseMessage,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: delete-user-by-email with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", responseMessage);
        futureResponse.complete(ResponseEntity.ok(responseMessage));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteUserByFullName(String userFullName) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: delete-user-by-full-name with correlation id: {} ", correlationId);
        ProducerRecord<String, String> topic = new ProducerRecord<>("delete-user-by-full-name", userFullName);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        stringKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "delete-user-by-full-name-response", groupId = "api-gateway",
            containerFactory = "stringKafkaListenerFactory")
    public void handleDeleteUserByFullNameResponse(String responseMessage,
                                                   @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: delete-user-by-full-name with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", responseMessage);
        futureResponse.complete(ResponseEntity.ok(responseMessage));
    }

    private CompletableFuture<ResponseEntity<Object>> getResponseEntityCompletableFuture(
            CompletableFuture<ResponseEntity<Object>> futureResponse) {
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
}
