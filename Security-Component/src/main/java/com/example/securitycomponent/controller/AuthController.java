package com.example.securitycomponent.controller;

import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.dto.AuthResponseDTO;
import com.example.securitycomponent.dto.JwksSpecificInfoDTO;
import com.example.securitycomponent.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/component")
    public ResponseEntity<AuthResponseDTO> authenticateComponent(@RequestBody AuthRequestDTO authRequestDTO) {
        LOGGER.debug("Received POST request to Authenticate Component with Credentials: {}", authRequestDTO);
        AuthResponseDTO authResponseDTO = authService.authenticateComponent(authRequestDTO);
        LOGGER.debug("Request was successfully processed and response was sent:{}", authResponseDTO);
        return ResponseEntity.ok(authResponseDTO);
    }

    @PostMapping("/jwks")
    public ResponseEntity<JwksSpecificInfoDTO> getJwks(@RequestBody AuthRequestDTO authRequestDTO) {
        LOGGER.debug("Received GET request to get actual Jwks with Credentials: {}", authRequestDTO);
        JwksSpecificInfoDTO jwksSpecificInfoDTO = authService.getJwks(authRequestDTO);
        LOGGER.debug("Request was successfully processed and response was sent: {}", jwksSpecificInfoDTO);
        return ResponseEntity.ok(jwksSpecificInfoDTO);
    }
}