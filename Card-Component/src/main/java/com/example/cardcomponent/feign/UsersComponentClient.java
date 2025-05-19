package com.example.cardcomponent.feign;

import com.example.cardcomponent.dto.UsersDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;
import java.util.UUID;

@Qualifier("Users-Components")
@FeignClient(name = "USERS-COMPONENTS", fallback = UsersComponentClientFallback.class)
public interface UsersComponentClient {
    @GetMapping("users/by-id/{userId}")
    @CircuitBreaker(name = "usersComponentCircuitBreaker", fallbackMethod = "usersComponentFallback")
    Optional<UsersDTO> findById(@PathVariable UUID userId);

    @GetMapping("users/by-name/{userFullName}")
    @CircuitBreaker(name = "usersComponentCircuitBreaker", fallbackMethod = "usersComponentFallback")
    Optional<UsersDTO> findByFullName(@PathVariable String userFullName);

    @GetMapping("/exists-by-id/{userId}")
    Optional<Boolean> existenceCheck(@PathVariable UUID userId);

    @GetMapping("/exists-by-name/{holderName}")
    Optional<Boolean> existenceCheck(@PathVariable String holderName);

    default UsersDTO usersComponentFallback(UUID usersId, Throwable ex) {
        return new UsersDTO();
    }
}
