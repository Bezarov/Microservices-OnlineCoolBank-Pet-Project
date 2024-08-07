package com.example.accountcomponent.client;

import com.example.accountcomponent.dto.UsersDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;
import java.util.UUID;

@Qualifier("Users-Component")
@FeignClient(name = "Users-Component", url = "http://localhost:8080/users", fallback = UsersComponentFallback.class)
public interface UsersComponent {
    @GetMapping("/by-id/{userId}")
    Optional<UsersDTO> findById(@PathVariable UUID userId);

    @GetMapping("/by-name/{userFullName}")
    Optional<UsersDTO> findByFullName(@PathVariable String userFullName);
}
