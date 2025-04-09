package com.example.apigatewaycomponent.controller;

import com.example.apigatewaycomponent.dto.UsersDTO;
import com.example.apigatewaycomponent.service.UsersGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users")
public class UsersGatewayController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsersGatewayController.class);
    private static final String RESPONSE_LOG = "Request was successfully processed and response was sent: {}";

    private final UsersGatewayService usersGatewayService;

    public UsersGatewayController(UsersGatewayService usersGatewayService) {
        this.usersGatewayService = usersGatewayService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<Object>> createUser(@RequestBody UsersDTO usersDTO) {
        LOGGER.debug("Received POST request to create User: {}", usersDTO);
        return usersGatewayService.createUser(usersDTO)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/by-id/{userId}")
    public CompletableFuture<ResponseEntity<Object>> getUserById(@PathVariable UUID userId) {
        SecurityContext context = SecurityContextHolder.getContext();
        LOGGER.debug("Received GET request to get User by ID: {}", userId);
        return usersGatewayService.getUserById(userId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    SecurityContextHolder.setContext(context);
                    LOGGER.debug(SecurityContextHolder.getContext().toString());
                    return response;
                });
    }

    @GetMapping("/by-email/{userEmail}")
    public CompletableFuture<ResponseEntity<Object>> getUserByEmail(@PathVariable String userEmail) {
        LOGGER.debug("Received GET request to get User by Email: {}", userEmail);
        return usersGatewayService.getUserByEmail(userEmail)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/by-name/{userFullName}")
    public CompletableFuture<ResponseEntity<Object>> getUserByFullName(@PathVariable String userFullName) {
        LOGGER.debug("Received GET request to get User by Full Name: {}", userFullName);
        return usersGatewayService.getUserByFullName(userFullName)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/by-phone/{userPhoneNumber}")
    public CompletableFuture<ResponseEntity<Object>> getUserByPhoneNumber(@PathVariable String userPhoneNumber) {
        LOGGER.debug("Received GET request to get User by Phone Number: {}", userPhoneNumber);
        return usersGatewayService.getUserByPhoneNumber(userPhoneNumber)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @PutMapping("/by-id/{userId}")
    public CompletableFuture<ResponseEntity<Object>> updateUser(@PathVariable UUID userId,
                                                                @RequestBody UsersDTO usersDTO) {
        LOGGER.debug("Received PUT request to update User with ID: {}, UPDATE TO: {}", userId, usersDTO);
        return usersGatewayService.updateUser(userId, usersDTO)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @PatchMapping("/by-id/{userId}/password/{newPassword}")
    public CompletableFuture<ResponseEntity<Object>> updatePasswordById(@PathVariable UUID userId,
                                                                        @PathVariable String newPassword) {
        LOGGER.debug("Received PATCH request to update User password with ID: {}, New Password: {}", userId, newPassword);
        return usersGatewayService.updatePasswordById(userId, newPassword)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @DeleteMapping("/by-id/{userId}")
    public CompletableFuture<ResponseEntity<Object>> deleteUserById(@PathVariable UUID userId) {
        LOGGER.debug("Received DELETE request to remove User with ID: {}", userId);
        return usersGatewayService.deleteUserById(userId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @DeleteMapping("/by-email/{userEmail}")
    public CompletableFuture<ResponseEntity<Object>> deleteUserByEmail(@PathVariable String userEmail) {
        LOGGER.debug("Received DELETE request to remove User with Email: {}", userEmail);
        return usersGatewayService.deleteUserByEmail(userEmail)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @DeleteMapping("/by-name/{userFullName}")
    public CompletableFuture<ResponseEntity<Object>> deleteUserByFullName(@PathVariable String userFullName) {
        LOGGER.debug("Received DELETE request to remove User with Full Name: {}", userFullName);
        return usersGatewayService.deleteUserByFullName(userFullName)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }
}
