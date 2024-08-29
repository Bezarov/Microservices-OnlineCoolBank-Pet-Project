package com.example.paymentcomponent.feign;

import com.example.paymentcomponent.dto.AccountDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AccountComponentClientFallback implements AccountComponentClient {
    @Override
    public Optional<AccountDTO> findById(UUID accountId) {
        return Optional.empty();
    }
}
