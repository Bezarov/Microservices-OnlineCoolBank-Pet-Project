package com.example.securitycomponent.service;

import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.exception.CustomKafkaException;
import com.example.securitycomponent.feign.AppRegistryComponentClient;
import com.example.securitycomponent.feign.UsersComponentClient;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AuthDetailsServiceImpl implements AuthDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthDetailsServiceImpl.class);
    private final UsersComponentClient usersComponentClient;
    private final AppRegistryComponentClient appRegistryComponentClient;

    public AuthDetailsServiceImpl(@Qualifier("Users-Components") UsersComponentClient usersComponentClient,
                                  @Qualifier("AppRegistry-Components") AppRegistryComponentClient appRegistryComponentClient) {
        this.usersComponentClient = usersComponentClient;
        this.appRegistryComponentClient = appRegistryComponentClient;
    }


    public void authenticateUser(AuthRequestDTO authRequestDTO) throws FeignException {
        LOGGER.info("Trying to find user with email: \"{}\"", authRequestDTO.principal());
        usersComponentClient.findByEmail(authRequestDTO.principal().toString())
                .filter(userEntity ->
                        userEntity.getEmail().equals(authRequestDTO.principal().toString()) &&
                                passwordEncoder().matches(authRequestDTO.credentials().toString(),
                                        userEntity.getPassword()))
                .orElseThrow(() -> {
                    LOGGER.error("User with such email not found: \"{}\"", authRequestDTO.principal());
                    return new CustomKafkaException(HttpStatus.UNAUTHORIZED,
                            "Authentication failed: invalid user credentials");
                });
        LOGGER.debug("User was found successfully");
    }

    public void authenticateComponent(AuthRequestDTO authRequestDTO) {
        LOGGER.info("Trying to find Component with ID: \"{}\"", authRequestDTO.principal());
        appRegistryComponentClient.findById(UUID.fromString(authRequestDTO.principal().toString()))
                .filter(componentEntity -> componentEntity.getComponentId().toString().equals(
                        authRequestDTO.principal().toString()) && passwordEncoder().matches(
                                authRequestDTO.credentials().toString(), componentEntity.getComponentSecret())
                )
                .orElseThrow(() -> {
                    LOGGER.error("Component not found: \"{}\"", authRequestDTO);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "Authentication failed: invalid component credentials");
                });
        LOGGER.debug("Component was found successfully");
        LOGGER.info("Component successfully authenticated: \"{}\"", authRequestDTO);
    }

    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
