package com.example.userscomponent.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class AccountComponentFallback implements AccountComponent {
    private static final Logger logger = LoggerFactory.getLogger(AccountComponentFallback.class);

    @Override
    public List<Object> getAccountsByUserId(UUID userId) {
        logger.error("Account-App-Component unreachable, returned empty Account List");
        return Collections.emptyList();
    }

    @Override
    public void deleteAllUsersAccounts(UUID userId) {
        logger.error("Account-Component unreachable");
    }
}
