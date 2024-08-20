package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.UsersDTO;
import com.example.apigatewaycomponent.errordto.UsersErrorDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UsersGatewayService {
    void handleUsersErrors(UsersErrorDTO usersErrorDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> createUser(@RequestBody UsersDTO usersDTO);

    void handleUserCreationResponse(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getUserById(String userId);
    void handleGetUserByIdResponse(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getUserByEmail(String userEmail);
    void handleGetUserByEmailResponse(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getUserByFullName(String userFullName);
    void handleGetUserByFullNameResponse(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getUserByPhoneNumber(String userPhoneNumber);
    void handleGetUserByPhoneNumberResponse(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updateUser(String userId, UsersDTO usersDTO);

    void handleUpdateUserByIdResponse(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updatePasswordById(String userId, String newPassword);
    void handleUpdateUserPassordByIdResponse(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    ResponseEntity<String> deleteUserById(UUID userId);

    ResponseEntity<String> deleteUserByEmail(String userEmail);

    ResponseEntity<String> deleteUserByFullName(String userFullName);
}
