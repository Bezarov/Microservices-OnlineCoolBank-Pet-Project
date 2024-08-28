package com.example.cardcomponent.feign;

import com.example.cardcomponent.dto.AccountDTO;
import com.example.cardcomponent.dto.UsersDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;
import java.util.UUID;

@Qualifier("Account-Components")
@FeignClient(name = "ACCOUNT-COMPONENTS", url = "http://localhost:8201/account", fallback = AccountComponentClientFallback.class)
public interface AccountComponentClient {
    @GetMapping("/by-account-id/{accountId}")
    @CircuitBreaker(name = "accountComponentCircuitBreaker", fallbackMethod = "accountComponentFallback")
    Optional<AccountDTO> findById(@PathVariable UUID accountId);

    default AccountDTO accountComponentFallback(UUID accountId, Throwable ex) {
        return new AccountDTO();
    }

}
