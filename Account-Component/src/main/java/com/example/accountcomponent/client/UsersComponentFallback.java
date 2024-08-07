package com.example.accountcomponent.client;

import com.example.accountcomponent.dto.UsersDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UsersComponentFallback implements UsersComponent {
    @Override
    public Optional<UsersDTO> findById(UUID userId) {
        return Optional.empty();
    }

    @Override
    public Optional<UsersDTO> findByFullName(String userFullName) {
        return Optional.empty();
    }
}
