package com.example.securitycomponent.service;

import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.jwt.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {
    private final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final AuthDetailsService authDetailsService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthServiceImpl(AuthDetailsService authDetailsService, JwtUtil jwtUtil) {
        this.authDetailsService = authDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String authenticateUser(AuthRequestDTO authRequestDTO) {
        try {
            logger.info("Authenticating user with email: {}", authRequestDTO.principal());
            authDetailsService.authenticateUser(authRequestDTO);
            logger.info("Authentication successfully for user with email: {}", authRequestDTO.principal());
            logger.info("Trying to generate user token for credentials: {}", authRequestDTO);
            String token = jwtUtil.userTokenGenerator(authRequestDTO.principal().toString());
            logger.info("Generated JWT Token: {}", token);
            return token;
        } catch (AuthenticationException error) {
            logger.error("Authentication failed for User with Email: {} Password: {}",
                    authRequestDTO.principal(), authRequestDTO.credentials());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Authentication failed \nInvalid Email or Password");
        }
    }

    @Override
    public String authenticateComponent(AuthRequestDTO authRequestDTO) {
        try {
            logger.info("Authenticating component with ID: {}", authRequestDTO.principal());
            authDetailsService.authenticateComponent(authRequestDTO);
            logger.info("Authentication successfully for Component with ID: {}", authRequestDTO.principal());

            logger.info("Trying to generate component token for credentials: {}", authRequestDTO);
            String token = jwtUtil.componentTokenGenerator(authRequestDTO.principal().toString());
            logger.info("Generated JWT Token: {}", token);
            return token;
        } catch (AuthenticationException error) {
            logger.error("Authentication failed for Component with ID: {} Secret: {}",
                    authRequestDTO, authRequestDTO);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Authentication failed \nInvalid Component ID or Secret");
        }
    }
}
