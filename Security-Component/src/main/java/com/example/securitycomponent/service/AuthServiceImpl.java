package com.example.securitycomponent.service;

import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.jwt.JwtTokenAuthenticator;
import com.example.securitycomponent.jwt.JwtTokenTypeAuthorizer;
import com.example.securitycomponent.jwt.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final AuthDetailsService authDetailsService;
    private final JwtUtil jwtUtil;

    private final JwtTokenAuthenticator jwtTokenAuthenticator;
    private final JwtTokenTypeAuthorizer jwtTokenTypeAuthorizer;

    @Autowired
    public AuthServiceImpl(AuthDetailsService authDetailsService, JwtUtil jwtUtil, JwtTokenAuthenticator
            jwtTokenAuthenticator, JwtTokenTypeAuthorizer jwtTokenTypeAuthorizer) {
        this.authDetailsService = authDetailsService;
        this.jwtUtil = jwtUtil;
        this.jwtTokenAuthenticator = jwtTokenAuthenticator;
        this.jwtTokenTypeAuthorizer = jwtTokenTypeAuthorizer;
    }

    @Override
    public String authenticateComponent(AuthRequestDTO authRequestDTO) {
        logger.info("Authenticating component with ID: {}", authRequestDTO.principal());
        authDetailsService.authenticateComponent(authRequestDTO);
        logger.info("Authentication successfully for Component with ID: {}", authRequestDTO.principal());

        logger.info("Trying to generate component token for credentials: {}", authRequestDTO);
        String jwtToken = jwtUtil.componentTokenGenerator(authRequestDTO.principal().toString());
        logger.info("Generated JWT Token: {}", jwtToken);
        return jwtToken;
    }

    @Override
    public Boolean authenticateComponentToken(String jwtToken, String requestURI) {
        logger.info("Authenticating component Token: {}", jwtToken);
        SecurityContext responseSecurityContext = jwtTokenAuthenticator.doTokenAuthentication(jwtToken);
        logger.info("Authentication successfully");

        logger.info("Authorizing user Token: {} and requested URI: {}", jwtToken, requestURI);
        jwtTokenTypeAuthorizer.doTokenAuthorization(jwtToken, requestURI);
        logger.info("Authentication and authorization successfully");
        return responseSecurityContext.getAuthentication().isAuthenticated();
    }
}
