package com.example.accountcomponent.feign;

import com.example.accountcomponent.dto.CardDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Qualifier("Card-Components")
@FeignClient(name = "CARD-COMPONENTS", fallback = CardComponentClientFallback.class)
public interface CardComponentClient {
    @GetMapping("card/by-account-id/{accountId}")
    @CircuitBreaker(name = "cardComponentCircuitBreaker")
    List<CardDTO> findAllCardsByAccountId(@PathVariable UUID accountId);

    @DeleteMapping("card/by-account-id/{accountId}")
    @CircuitBreaker(name = "cardComponentCircuitBreaker")
    void deleteAllAccountCardsByAccountId(@PathVariable UUID accountId);
}
