package com.example.apigatewaycomponent.controller;

import com.example.apigatewaycomponent.dto.UsersDTO;
import com.example.apigatewaycomponent.service.UsersGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        CompletableFuture<ResponseEntity<Object>> responseUsersDTO = usersGatewayService.createUser(usersDTO);
        logger.debug("Request was successfully processed and response was sent: {}", responseUsersDTO);
        return responseUsersDTO;
    }

    @GetMapping("/by-id/{userId}")
    public CompletableFuture<ResponseEntity<Object>> getUserById(@PathVariable String userId) {
        logger.info("Received GET request to get User by ID: {}", userId);
        CompletableFuture<ResponseEntity<Object>> responseUsersDTO = usersGatewayService.getUserById(userId);
        logger.debug("Request was successfully processed and response was sent: {}", responseUsersDTO);
        return responseUsersDTO;
    }

    @GetMapping("/by-email/{userEmail}")
    public CompletableFuture<ResponseEntity<Object>> getUserByEmail(@PathVariable String userEmail) {
        logger.info("Received GET request to get User by Email: {}", userEmail);
        CompletableFuture<ResponseEntity<Object>> responseUsersDTO = usersGatewayService.getUserByEmail(userEmail);
        logger.debug("Request was successfully processed and response was sent: {}", responseUsersDTO);
        return responseUsersDTO;
    }

    @GetMapping("/by-name/{userFullName}")
    public CompletableFuture<ResponseEntity<Object>> getUserByFullName(@PathVariable String userFullName) {
        logger.info("Received GET request to get User by Full Name: {}", userFullName);
        CompletableFuture<ResponseEntity<Object>> responseUsersDTO = usersGatewayService.getUserByFullName(userFullName);
        logger.debug("Request was successfully processed and response was sent: {}", responseUsersDTO);
        return responseUsersDTO;
    }

    @GetMapping("/by-phone/{userPhoneNumber}")
    public CompletableFuture<ResponseEntity<Object>> getUserByPhoneNumber(@PathVariable String userPhoneNumber) {
        logger.info("Received GET request to get User by Phone Number: {}", userPhoneNumber);
        CompletableFuture<ResponseEntity<Object>> responseUsersDTO = usersGatewayService.getUserByPhoneNumber(userPhoneNumber);
        logger.debug("Request was successfully processed and response was sent: {}", responseUsersDTO);
        return responseUsersDTO;
    }

    @PutMapping("/by-id/{userId}")
    public CompletableFuture<ResponseEntity<Object>> updateUser(@PathVariable String userId,
                                               @RequestBody UsersDTO usersDTO) {
        logger.info("Received PUT request to update User with ID: {}," +
                " UPDATE TO: {}", userId, usersDTO);
        CompletableFuture<ResponseEntity<Object>> responseUsersDTO = usersGatewayService.updateUser(userId, usersDTO);
        logger.debug("Request was successfully processed and response was sent: {}", responseUsersDTO);
        return responseUsersDTO;
    }

    @PatchMapping("/by-id/{userId}/password/{newPassword}")
    public CompletableFuture<ResponseEntity<Object>> updatePasswordById(@PathVariable String userId,
                                                       @PathVariable String newPassword) {
        logger.info("Received PATCH request to update User password with ID: {}," +
                " New Password: {}", userId, newPassword);
        CompletableFuture<ResponseEntity<Object>> responseUsersDTO = usersGatewayService.updatePasswordById(userId, newPassword);
        logger.debug("Request was successfully processed and response was sent: {}", responseUsersDTO);
        return responseUsersDTO;
    }
//
//    @DeleteMapping("/by-id/{userId}")
//    public ResponseEntity<String> deleteUserById(@PathVariable UUID userId) {
//        logger.info("Received DELETE request to remove User with ID: {}", userId);
//        ResponseEntity<String> responseMessage = usersService.deleteUserById(userId);
//        logger.debug("Request was successfully processed and response message was sent: {}", responseMessage);
//        return responseMessage;
//    }
//
//    @DeleteMapping("/by-email/{userEmail}")
//    public ResponseEntity<String> deleteUserByEmail(@PathVariable String userEmail) {
//        logger.info("Received DELETE request to remove User with Email: {}", userEmail);
//        ResponseEntity<String> responseMessage = usersService.deleteUserByEmail(userEmail);
//        logger.debug("Request was successfully processed and response message was sent: {}", responseMessage);
//        return responseMessage;
//    }
//
//    @DeleteMapping("/by-name/{userFullName}")
//    public ResponseEntity<String> deleteUserByFullName(@PathVariable String userFullName) {
//        logger.info("Received DELETE request to remove User with Full Name: {}", userFullName);
//        ResponseEntity<String> responseMessage = usersService.deleteUserByFullName(userFullName);
//        logger.debug("Request was successfully processed and response message was sent: {}", responseMessage);
//        return responseMessage;
//    }
}
