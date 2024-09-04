package com.example.securitycomponent.controller;

import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.dto.AuthResponseDTO;
import com.example.securitycomponent.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/component")
    public ResponseEntity<String> authenticateComponent(@RequestBody AuthRequestDTO authRequestDTO) {
        logger.info("Received POST request to Authenticate Component with Credentials: {}", authRequestDTO);
        String responseToken = authService.authenticateComponent(authRequestDTO);
        logger.debug("Request was successfully processed and response was sent: Token={}", responseToken);
        return ResponseEntity.ok(new AuthResponseDTO(responseToken).toString());
    }

    @PostMapping("/component/token")
    public ResponseEntity<Boolean> authenticateComponentToken(@RequestBody String jwtToken, String requestURI) {
        logger.info("Received GET request to Authenticate Component token: {}", jwtToken);
        return ResponseEntity.ok(authService.authenticateComponentToken(jwtToken, requestURI));
    }
}