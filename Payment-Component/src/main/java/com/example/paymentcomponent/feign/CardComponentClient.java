package com.example.paymentcomponent.feign;

import com.example.paymentcomponent.dto.CardDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Qualifier("Card-Components")
@FeignClient(name = "CARD-COMPONENTS", fallback = CardComponentClientFallback.class)
public interface CardComponentClient {
    Logger logger = LoggerFactory.getLogger(CardComponentClient.class);

    @GetMapping("card/by-card-id/{cardId}")
    @CircuitBreaker(name = "cardComponentCircuitBreaker", fallbackMethod = "cardComponentFallback")
    List<CardDTO> findAllCardsByAccountId(@PathVariable UUID cardId);

    @GetMapping("card/by-card-number/{cardNumber}")
    @CircuitBreaker(name = "cardComponentCircuitBreaker", fallbackMethod = "cardComponentFallbackByCardNumber")
    Optional<CardDTO> findByCardNumber(@PathVariable String cardNumber);

    default CardDTO cardComponentFallback(UUID accountId, Throwable ex) {
        return new CardDTO();
    }

    default ResponseStatusException cardComponentFallbackByCardNumber(String cardNumber) {
        logger.error("Card Component is unreachable operation aborted generate exception");
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Service is unreachable please try again later.");
    }
}
