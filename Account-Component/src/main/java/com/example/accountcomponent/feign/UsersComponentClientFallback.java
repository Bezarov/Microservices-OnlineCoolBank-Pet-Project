package com.example.accountcomponent.feign;

import com.example.accountcomponent.dto.UsersDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UsersComponentClientFallback implements UsersComponentClient {
    @Override
    public Optional<UsersDTO> findById(UUID userId) {
        return Optional.empty();
    }

    @Override
    public Optional<UsersDTO> findByFullName(String userFullName) {
        return Optional.empty();
    }
}
