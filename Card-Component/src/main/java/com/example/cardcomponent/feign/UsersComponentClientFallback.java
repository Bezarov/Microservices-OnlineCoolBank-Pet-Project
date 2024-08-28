package com.example.cardcomponent.feign;

import com.example.cardcomponent.dto.UsersDTO;
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
