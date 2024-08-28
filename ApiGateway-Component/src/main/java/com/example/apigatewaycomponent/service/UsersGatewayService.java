package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.UsersDTO;
import com.example.apigatewaycomponent.dto.ErrorDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UsersGatewayService {
    void handleUsersErrors(ErrorDTO usersErrorDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> createUser(@RequestBody UsersDTO usersDTO);

    void handleUserCreationResponse(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getUserById(UUID userId);

    void handleGetUserByIdResponse(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getUserByEmail(String userEmail);

    void handleGetUserByEmailResponse(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getUserByFullName(String userFullName);

    void handleGetUserByFullNameResponse(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getUserByPhoneNumber(String userPhoneNumber);

    void handleGetUserByPhoneNumberResponse(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updateUser(UUID userId, UsersDTO usersDTO);

    void handleUpdateUserByIdResponse(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updatePasswordById(UUID userId, String newPassword);

    void handleUpdateUserPasswordByIdResponse(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> deleteUserById(UUID userId);

    void handleDeleteUserByIdResponse(String responseMessage, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> deleteUserByEmail(String userEmail);

    void handleDeleteUserByEmailResponse(String responseMessage, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> deleteUserByFullName(String userFullName);

    void handleDeleteUserByFullNameResponse(String responseMessage, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);
}
