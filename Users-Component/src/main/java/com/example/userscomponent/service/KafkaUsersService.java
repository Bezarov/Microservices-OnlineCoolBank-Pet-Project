package com.example.userscomponent.service;

import com.example.userscomponent.dto.UsersDTO;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.util.Map;
import java.util.UUID;

public interface KafkaUsersService {

    void createUser(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getUserById(UUID userId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getUserByEmail(String userEmail, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getUserByFullName(String userFullName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getUserByPhoneNumber(String userPhoneNumber, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void updateUser(Map<String, Object> UUIDAndUserDTOMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void updatePasswordById(Map<String, String> UUIDAndNewPasswordMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void deleteUserById(String userId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void deleteUserByEmail(String userEmail, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void deleteUserByFullName(String userFullName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);
}
