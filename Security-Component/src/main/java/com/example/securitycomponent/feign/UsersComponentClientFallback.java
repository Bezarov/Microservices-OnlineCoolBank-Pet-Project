package com.example.securitycomponent.feign;

import com.example.securitycomponent.dto.UsersDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Component
public class UsersComponentClientFallback implements UsersComponentClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsersComponentClientFallback.class);

    @Override
    public Optional<UsersDTO> findByEmail(String userEmail) {
        usersComponentFallback(userEmail);
        return Optional.empty();
    }


    @Override
    public void usersComponentFallback(String userEmail) {
        LOGGER.error("Users Component is unreachable authentication failed for user with email: {}" +
                " generate exception", userEmail);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Service is unreachable please try again later.");
    }
}
