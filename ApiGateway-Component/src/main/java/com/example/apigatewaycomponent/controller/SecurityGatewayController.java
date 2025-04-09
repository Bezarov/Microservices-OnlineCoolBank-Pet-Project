package com.example.apigatewaycomponent.controller;

import com.example.apigatewaycomponent.dto.AuthRequestDTO;
import com.example.apigatewaycomponent.service.SecurityGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/auth")
public class SecurityGatewayController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityGatewayController.class);
    private final SecurityGatewayService securityGatewayService;

    public SecurityGatewayController(SecurityGatewayService securityGatewayService) {
        this.securityGatewayService = securityGatewayService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<Object>> authenticateUser(@RequestBody AuthRequestDTO authRequestDTO) {
        LOGGER.debug("Received POST request to Authenticate User with Credentials: {}", authRequestDTO);
        return securityGatewayService.authenticateUser(authRequestDTO)
                .thenApply(response -> {
                    LOGGER.debug("Request was successfully processed and response was sent: Token={}", response);
                    return ResponseEntity.ok("Authentication successfully!" +
                            "\nPlease use this JWT Token for further Access \n" + response.getBody());
                });
    }
}
