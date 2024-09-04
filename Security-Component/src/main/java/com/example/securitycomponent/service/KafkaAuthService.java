package com.example.securitycomponent.service;

import com.example.securitycomponent.dto.AuthRequestDTO;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.util.Map;

public interface KafkaAuthService {
    void authenticateUser(AuthRequestDTO authRequestDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void authenticateUserToken(Map<String, String> mapJwtTokenToRequestURI, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);
}
