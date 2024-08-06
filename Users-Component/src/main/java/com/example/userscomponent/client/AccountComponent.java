package com.example.userscomponent.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Qualifier("Account-Component")
@FeignClient(name = "Account-Component", url = "http://localhost:8090", fallback = AccountComponentFallback.class)
public interface AccountComponent {
    @GetMapping("/accounts/by-user-id/{userId}")
    List<Object> getAccountsByUserId(@PathVariable("userId") UUID userId);

    @DeleteMapping("/accounts/by-user-id/{userId}")
    void deleteAllUsersAccounts(@PathVariable("userId") UUID userId);
}
