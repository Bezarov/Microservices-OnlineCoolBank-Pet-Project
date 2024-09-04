package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.AuthRequestDTO;
import com.example.apigatewaycomponent.dto.AuthResponseDTO;
import com.example.apigatewaycomponent.dto.ErrorDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.concurrent.CompletableFuture;

public interface SecurityGatewayService {
    void handleSecurityErrors(ErrorDTO securityErrorDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> authenticateUser(AuthRequestDTO authRequestDTO);

    void handleUserAuthenticationResponse(AuthResponseDTO authResponseDTO,
                                          @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    public CompletableFuture<ResponseEntity<Object>> authenticateUserToken(String jwtToken, String requestURI);

    void handleUserTokenAuthenticationResponse(SecurityContextImpl securityContext,
                                               @Header(KafkaHeaders.CORRELATION_ID) String correlationId);
}
