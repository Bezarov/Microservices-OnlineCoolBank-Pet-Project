package com.example.userscomponent.controller;

import com.example.userscomponent.dto.UsersDTO;
import com.example.userscomponent.service.RestUsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UsersController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsersController.class);
    private static final String RESPONSE_LOG = "Request was successfully processed and response was sent: {}";

    private final RestUsersService restUsersService;

    public UsersController(RestUsersService restUsersService) {
        this.restUsersService = restUsersService;
    }

    @GetMapping("/by-id/{userId}")
    public ResponseEntity<UsersDTO> getUserById(@PathVariable UUID userId) {
        LOGGER.debug("Received GET request to get User by ID: {}", userId);
        UsersDTO usersDTO = restUsersService.getUserById(userId);
        LOGGER.debug(RESPONSE_LOG, usersDTO);
        return ResponseEntity.ok(usersDTO);
    }

    @GetMapping("/by-email/{userEmail}")
    public ResponseEntity<UsersDTO> getUserByEmail(@PathVariable String userEmail) {
        LOGGER.debug("Received GET request to get User by Email: {}", userEmail);
        UsersDTO usersDTO = restUsersService.getUserByEmail(userEmail);
        LOGGER.debug(RESPONSE_LOG, usersDTO);
        return ResponseEntity.ok(usersDTO);
    }

    @GetMapping("/by-name/{userFullName}")
    public ResponseEntity<UsersDTO> getUserByFullName(@PathVariable String userFullName) {
        LOGGER.debug("Received GET request to get User by Full Name: {}", userFullName);
        UsersDTO usersDTO = restUsersService.getUserByFullName(userFullName);
        LOGGER.debug(RESPONSE_LOG, usersDTO);
        return ResponseEntity.ok(usersDTO);
    }

    @GetMapping("/by-phone/{userPhoneNumber}")
    public ResponseEntity<UsersDTO> getUserByPhoneNumber(@PathVariable String userPhoneNumber) {
        LOGGER.debug("Received GET request to get User by Phone Number: {}", userPhoneNumber);
        UsersDTO usersDTO = restUsersService.getUserByPhoneNumber(userPhoneNumber);
        LOGGER.debug(RESPONSE_LOG, usersDTO);
        return ResponseEntity.ok(usersDTO);
    }

    @GetMapping("/by-id/name/{userId}")
    public ResponseEntity<String> getNameById(@PathVariable UUID userId) {
        LOGGER.debug("Received GET request to get User name by ID: {}", userId);
        String name = restUsersService.getFullNameById(userId);
        LOGGER.debug(RESPONSE_LOG, name);
        return ResponseEntity.ok(name);
    }
}