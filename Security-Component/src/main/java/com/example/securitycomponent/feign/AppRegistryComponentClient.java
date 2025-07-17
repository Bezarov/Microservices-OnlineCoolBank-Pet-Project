package com.example.securitycomponent.feign;

import com.example.securitycomponent.dto.AppComponentDTO;
import com.example.securitycomponent.config.SecurityAppComponentConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Qualifier("AppRegistry-Components")
@FeignClient(name = "APPREGISTRY-COMPONENTS", url = "http://localhost:8601/component", fallback = AppRegistryComponentClientFallback.class)
public interface AppRegistryComponentClient {
    Logger logger = LoggerFactory.getLogger(UsersComponentClient.class);

    @PostMapping
    void registerComponent(@RequestBody SecurityAppComponentConfig securityAppComponentConfig);

    @GetMapping("/by-id/{componentId}")
    @CircuitBreaker(name = "appRegistryComponentCircuitBreaker", fallbackMethod = "appRegistryComponentFallback")
    Optional<AppComponentDTO> findById(@PathVariable UUID componentId);

    @DeleteMapping("/by-id/{componentId}")
    ResponseEntity<String> deregisterComponent(@PathVariable UUID componentId);

    default void appRegistryComponentFallback(UUID componentId) {
        logger.error("To many errors authentication failed for component with id: {}  generate exception", componentId);
        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Too many requests, please try again later.");
    }
}
