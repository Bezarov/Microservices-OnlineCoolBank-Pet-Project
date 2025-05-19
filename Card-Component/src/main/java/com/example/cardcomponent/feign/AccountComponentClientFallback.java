package com.example.cardcomponent.feign;

import com.example.cardcomponent.dto.AccountDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AccountComponentClientFallback implements AccountComponentClient {
    @Override
    public Optional<AccountDTO> findById(UUID accountId) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> existenceCheck(UUID accountId) {
        return Optional.empty();
    }

    @Override
    public AccountDTO accountComponentFallback(UUID accountId, Throwable ex) {
        return AccountComponentClient.super.accountComponentFallback(accountId, ex);
    }
}
