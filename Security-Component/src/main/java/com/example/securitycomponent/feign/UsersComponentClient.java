package com.example.securitycomponent.feign;

import com.example.securitycomponent.dto.UsersDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Qualifier("Users-Components")
@FeignClient(name = "USERS-COMPONENTS", fallback = UsersComponentClientFallback.class)
public interface UsersComponentClient {
    Logger logger = LoggerFactory.getLogger(UsersComponentClient.class);

    @GetMapping("users/by-email/{userEmail}")
    @CircuitBreaker(name = "usersComponentCircuitBreaker", fallbackMethod = "usersComponentFallback")
    Optional<UsersDTO> findByEmail(@PathVariable String userEmail);

    default void usersComponentFallback(String userEmail) {
        logger.error("To many errors authentication failed for user with email: {}  generate exception", userEmail);
        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Too many requests, please try again later.");
    }
}
