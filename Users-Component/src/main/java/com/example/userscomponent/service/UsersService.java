package com.example.userscomponent.service;

import com.example.userscomponent.dto.UsersDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.util.UUID;

public interface UsersService {

    void createUser(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    UsersDTO getUserById(UUID userId);

    UsersDTO getUserByEmail(String userEmail);

    UsersDTO getUserByFullName(String userFullName);

    UsersDTO getUserByPhoneNumber(String userPhoneNumber);

    UsersDTO updateUser(UUID userId, UsersDTO usersDTO);

    UsersDTO updatePasswordById(UUID userId, String newPassword);

    ResponseEntity<String> deleteUserById(UUID userId);

    ResponseEntity<String> deleteUserByEmail(String userEmail);

    ResponseEntity<String> deleteUserByFullName(String userFullName);
}
