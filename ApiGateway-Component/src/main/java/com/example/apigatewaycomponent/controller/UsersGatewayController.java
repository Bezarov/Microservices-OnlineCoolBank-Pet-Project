package com.example.apigatewaycomponent.controller;

import com.example.apigatewaycomponent.dto.UsersDTO;
import com.example.apigatewaycomponent.service.UsersGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users")
public class UsersGatewayController {
    private static final Logger logger = LoggerFactory.getLogger(UsersGatewayController.class);
    private final UsersGatewayService usersGatewayService;

    public UsersGatewayController(UsersGatewayService usersGatewayService) {
        this.usersGatewayService = usersGatewayService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<Object>> createUser(@RequestBody UsersDTO usersDTO) {
        logger.info("Received POST request to create User: {}", usersDTO);
        return usersGatewayService.createUser(usersDTO)
                .thenApply(response -> {
                    logger.debug("Request was successfully processed and response was sent: {}", response);
                    return response;
                });
    }

    @GetMapping("/by-id/{userId}")
    public CompletableFuture<ResponseEntity<Object>> getUserById(@PathVariable UUID userId) {
        logger.info("Received GET request to get User by ID: {}", userId);
        return usersGatewayService.getUserById(userId)
                .thenApply(response -> {
                    logger.debug("Request was successfully processed and response was sent: {}", response);
                    return response;
                });
    }

    @GetMapping("/by-email/{userEmail}")
    public CompletableFuture<ResponseEntity<Object>> getUserByEmail(@PathVariable String userEmail) {
        logger.info("Received GET request to get User by Email: {}", userEmail);
        return usersGatewayService.getUserByEmail(userEmail)
                .thenApply(response -> {
                    logger.debug("Request was successfully processed and response was sent: {}", response);
                    return response;
                });
    }

    @GetMapping("/by-name/{userFullName}")
    public CompletableFuture<ResponseEntity<Object>> getUserByFullName(@PathVariable String userFullName) {
        logger.info("Received GET request to get User by Full Name: {}", userFullName);
        return usersGatewayService.getUserByFullName(userFullName)
                .thenApply(response -> {
                    logger.debug("Request was successfully processed and response was sent: {}", response);
                    return response;
                });
    }

    @GetMapping("/by-phone/{userPhoneNumber}")
    public CompletableFuture<ResponseEntity<Object>> getUserByPhoneNumber(@PathVariable String userPhoneNumber) {
        logger.info("Received GET request to get User by Phone Number: {}", userPhoneNumber);
        return usersGatewayService.getUserByPhoneNumber(userPhoneNumber)
                .thenApply(response -> {
                    logger.debug("Request was successfully processed and response was sent: {}", response);
                    return response;
                });
    }

    @PutMapping("/by-id/{userId}")
    public CompletableFuture<ResponseEntity<Object>> updateUser(@PathVariable String userId,
                                                                @RequestBody UsersDTO usersDTO) {
        logger.info("Received PUT request to update User with ID: {}, UPDATE TO: {}", userId, usersDTO);
        return usersGatewayService.updateUser(userId, usersDTO)
                .thenApply(response -> {
                    logger.debug("Request was successfully processed and response was sent: {}", response);
                    return response;
                });
    }

    @PatchMapping("/by-id/{userId}/password/{newPassword}")
    public CompletableFuture<ResponseEntity<Object>> updatePasswordById(@PathVariable String userId,
                                                                        @PathVariable String newPassword) {
        logger.info("Received PATCH request to update User password with ID: {}, New Password: {}", userId, newPassword);
        return usersGatewayService.updatePasswordById(userId, newPassword)
                .thenApply(response -> {
                    logger.debug("Request was successfully processed and response was sent: {}", response);
                    return response;
                });
    }

    @DeleteMapping("/by-id/{userId}")
    public CompletableFuture<ResponseEntity<Object>> deleteUserById(@PathVariable String userId) {
        logger.info("Received DELETE request to remove User with ID: {}", userId);
        return usersGatewayService.deleteUserById(userId)
                .thenApply(response -> {
                    logger.debug("Request was successfully processed and response was sent: {}", response);
                    return response;
                });
    }

    @DeleteMapping("/by-email/{userEmail}")
    public CompletableFuture<ResponseEntity<Object>> deleteUserByEmail(@PathVariable String userEmail) {
        logger.info("Received DELETE request to remove User with Email: {}", userEmail);
        return usersGatewayService.deleteUserByEmail(userEmail)
                .thenApply(response -> {
                    logger.debug("Request was successfully processed and response was sent: {}", response);
                    return response;
                });
    }

    @DeleteMapping("/by-name/{userFullName}")
    public CompletableFuture<ResponseEntity<Object>> deleteUserByFullName(@PathVariable String userFullName) {
        logger.info("Received DELETE request to remove User with Full Name: {}", userFullName);
        return usersGatewayService.deleteUserByFullName(userFullName)
                .thenApply(response -> {
                    logger.debug("Request was successfully processed and response was sent: {}", response);
                    return response;
                });
    }
}
