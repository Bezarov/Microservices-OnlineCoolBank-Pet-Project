package com.example.securitycomponent.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class JwtTokenTypeAuthorizer {
    private final Logger logger = LoggerFactory.getLogger(JwtTokenTypeAuthorizer.class);
    private final JwtUtil jwtUtil;

    public JwtTokenTypeAuthorizer(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public void doTokenAuthorization(String jwtToken, String requestURI) {
        logger.info("Extracting token type");
        String jwtTokenType = jwtUtil.extractClaim(jwtToken, claims -> claims.get("tokenType", String.class));
        logger.info("Authorizing extracted token type and requested URI");
        if (requestURI.startsWith("/component") && !"component".equals(jwtTokenType)) {
            logger.error("Extracted token type: {}, access to resource: {}  denied", jwtTokenType, requestURI);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization failed");
        } else if (!requestURI.startsWith("/component") && !"user".equals(jwtTokenType)) {
            logger.error("Extracted token type: {}, access to resource: {}  denied", jwtTokenType, requestURI);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization failed");
        }
        logger.info("Authorization successfully for token: {}, with type: {}, to resource: {}", jwtToken, jwtTokenType, requestURI);
    }
}
