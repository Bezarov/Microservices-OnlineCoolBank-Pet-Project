package com.example.securitycomponent.service;

import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.model.AppComponent;
import com.example.securitycomponent.model.Users;
import com.example.securitycomponent.repository.AppComponentRepository;
import com.example.securitycomponent.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AuthDetailsServiceImpl implements AuthDetailsService {
    private final Logger logger = LoggerFactory.getLogger(AuthDetailsServiceImpl.class);
    private final UsersRepository userRepository;
    private final AppComponentRepository componentRepository;

    public AuthDetailsServiceImpl(UsersRepository userRepository, AppComponentRepository componentRepository) {
        this.userRepository = userRepository;
        this.componentRepository = componentRepository;
    }

    public void authenticateUser(AuthRequestDTO authRequestDTO) {
        logger.info("Trying to find user with email: {}", authRequestDTO.principal());
        userRepository.findByEmail(authRequestDTO.principal().toString())
                .filter(UserEntity ->
                        UserEntity.getEmail().equals(authRequestDTO.principal().toString()) &&
                                passwordEncoder().matches(authRequestDTO.credentials().toString(),
                                        UserEntity.getPassword()))
                .orElseThrow(() -> {
                    logger.error("User with such email not found: {}", authRequestDTO.principal());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "Authentication failed: invalid user credentials");
                });

        logger.debug("User was found successfully");
        logger.info("User successfully authenticated: {}", authRequestDTO);
    }

    public Users authenticateUserToken(String principal) {
        logger.info("Trying to find User with email: {}", principal);
        Users user = userRepository.findByEmail(principal)
                .orElseThrow(() -> {
                    logger.error("User email extracted from the token was not found: {} ", principal);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "Authentication failed: invalid user token credentials");
                });

        logger.debug("User was found successfully");
        logger.info("Extracted user from token successfully authenticated");
        return user;
    }

    public void authenticateComponent(AuthRequestDTO authRequestDTO) {
        logger.info("Trying to find Component with ID: {}", authRequestDTO.principal());
        componentRepository.findById(UUID.fromString(authRequestDTO.principal().toString()))
                .filter(componentEntity -> componentEntity.getComponentId().toString().equals(
                        authRequestDTO.principal().toString()) &&
                        passwordEncoder().matches(
                                authRequestDTO.credentials().toString(), componentEntity.getComponentSecret())
                )
                .orElseThrow(() -> {
                    logger.error("Component not found: {}", authRequestDTO);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "Authentication failed: invalid component credentials");
                });
        logger.debug("Component was found successfully");
        logger.info("Component successfully authenticated: {}", authRequestDTO);
    }

    public AppComponent authenticateComponentToken(String principal) {
        logger.info("Trying to find extracted Component ID from token: {}", principal);
        AppComponent component = componentRepository.findById(UUID.fromString(principal))
                .orElseThrow(() -> {
                    logger.error("Component ID extracted from the Token was not found: {} ", principal);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "Authentication failed: invalid component token credentials");
                });
        logger.debug("Component was found successfully");
        logger.info("Extracted component from token successfully authenticated");
        return component;
    }

    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
