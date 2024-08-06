package com.example.userscomponent.controller;

import com.example.userscomponent.dto.UsersDTO;
import com.example.userscomponent.service.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/users")
public class UsersController {
    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);
    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping
    public ResponseEntity<UsersDTO> createUser(@RequestBody UsersDTO usersDTO) {
        logger.info("Received POST request to create User: {}", usersDTO);
        UsersDTO responseUsersDTO = usersService.createUser(usersDTO);
        logger.debug("Request was successfully processed and response was sent: {}", responseUsersDTO);
        return ResponseEntity.ok(responseUsersDTO);
    }

    @GetMapping("/by-id/{userId}")
    public ResponseEntity<UsersDTO> getUserById(@PathVariable UUID userId) {
        logger.info("Received GET request to get User by ID: {}", userId);
        UsersDTO usersDTO = usersService.getUserById(userId);
        logger.debug("Request was successfully processed and response was sent: {}", usersDTO);
        return ResponseEntity.ok(usersDTO);
    }

    @GetMapping("/by-email/{userEmail}")
    public ResponseEntity<UsersDTO> getUserByEmail(@PathVariable String userEmail) {
        logger.info("Received GET request to get User by Email: {}", userEmail);
        UsersDTO usersDTO = usersService.getUserByEmail(userEmail);
        logger.debug("Request was successfully processed and response was sent: {}", usersDTO);
        return ResponseEntity.ok(usersDTO);
    }

    @GetMapping("/by-name/{userFullName}")
    public ResponseEntity<UsersDTO> getUserByFullName(@PathVariable String userFullName) {
        logger.info("Received GET request to get User by Full Name: {}", userFullName);
        UsersDTO usersDTO = usersService.getUserByFullName(userFullName);
        logger.debug("Request was successfully processed and response was sent: {}", usersDTO);
        return ResponseEntity.ok(usersDTO);
    }

    @GetMapping("/by-phone/{userPhoneNumber}")
    public ResponseEntity<UsersDTO> getUserByPhoneNumber(@PathVariable String userPhoneNumber) {
        logger.info("Received GET request to get User by Phone Number: {}", userPhoneNumber);
        UsersDTO usersDTO = usersService.getUserByPhoneNumber(userPhoneNumber);
        logger.debug("Request was successfully processed and response was sent: {}", usersDTO);
        return ResponseEntity.ok(usersDTO);
    }

    @PutMapping("/by-id/{userId}")
    public ResponseEntity<UsersDTO> updateUser(@PathVariable UUID userId,
                                               @RequestBody UsersDTO usersDTO) {
        logger.info("Received PUT request to update User with ID: {}," +
                " UPDATE TO: {}", userId, usersDTO);
        UsersDTO responseUsersDTO = usersService.updateUser(userId, usersDTO);
        logger.debug("Request was successfully processed and response was sent: {}", responseUsersDTO);
        return ResponseEntity.ok(responseUsersDTO);
    }

    @PatchMapping("/by-id/{userId}/password/{newPassword}")
    public ResponseEntity<UsersDTO> updatePasswordById(@PathVariable UUID userId,
                                                       @PathVariable String newPassword) {
        logger.info("Received PATCH request to update User password with ID: {}," +
                " New Password: {}", userId, newPassword);
        UsersDTO responseUsersDTO = usersService.updatePasswordById(userId, newPassword);
        logger.debug("Request was successfully processed and response was sent: {}", responseUsersDTO);
        return ResponseEntity.ok(responseUsersDTO);
    }

    @DeleteMapping("/by-id/{userId}")
    public ResponseEntity<String> deleteUserById(@PathVariable UUID userId) {
        logger.info("Received DELETE request to remove User with ID: {}", userId);
        ResponseEntity<String> responseMessage = usersService.deleteUserById(userId);
        logger.debug("Request was successfully processed and response message was sent: {}", responseMessage);
        return responseMessage;
    }

    @DeleteMapping("/by-email/{userEmail}")
    public ResponseEntity<String> deleteUserByEmail(@PathVariable String userEmail) {
        logger.info("Received DELETE request to remove User with Email: {}", userEmail);
        ResponseEntity<String> responseMessage = usersService.deleteUserByEmail(userEmail);
        logger.debug("Request was successfully processed and response message was sent: {}", responseMessage);
        return responseMessage;
    }

    @DeleteMapping("/by-name/{userFullName}")
    public ResponseEntity<String> deleteUserByFullName(@PathVariable String userFullName) {
        logger.info("Received DELETE request to remove User with Full Name: {}", userFullName);
        ResponseEntity<String> responseMessage = usersService.deleteUserByFullName(userFullName);
        logger.debug("Request was successfully processed and response message was sent: {}", responseMessage);
        return responseMessage;
    }
}