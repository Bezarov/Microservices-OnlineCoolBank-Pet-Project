package com.example.securitycomponent.controller;

import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.dto.AuthResponseDTO;
import com.example.securitycomponent.dto.TokenAuthRequestDTO;
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
    public ResponseEntity<String> authenticateComponent(@RequestBody AuthRequestDTO authRequestDTO) {
        LOGGER.debug("Received POST request to Authenticate Component with Credentials: {}", authRequestDTO);
        String responseToken = authService.authenticateComponent(authRequestDTO);
        LOGGER.debug("Request was successfully processed and response was sent: Token={}", responseToken);
        return ResponseEntity.ok(new AuthResponseDTO(responseToken).toString());
    }

    @PostMapping("/component/token")
    public ResponseEntity<Boolean> authenticateComponentToken(@RequestBody TokenAuthRequestDTO tokenAuthRequestDTO) {
        LOGGER.debug("Received POST request to Authenticate Component token: {}", tokenAuthRequestDTO);
        return ResponseEntity.ok(authService.authenticateComponentToken(tokenAuthRequestDTO));
    }
}