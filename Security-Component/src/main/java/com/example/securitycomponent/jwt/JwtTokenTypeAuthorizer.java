package com.example.securitycomponent.jwt;

import com.example.securitycomponent.dto.TokenAuthRequestDTO;
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

    public void doTokenAuthorization(TokenAuthRequestDTO tokenAuthRequestDTO) {
        logger.info("Extracting token type");
        String jwtTokenType = jwtUtil.extractClaim(tokenAuthRequestDTO.jwtToken(), claims -> claims.get("tokenType", String.class));
        logger.info("Extracted token type is: {}", jwtTokenType);
        if (tokenAuthRequestDTO.requestURI().startsWith("/component") && !"component".equals(jwtTokenType)) {
            logger.error("Extracted token type: {}, access to resource: {}  denied",
                    jwtTokenType, tokenAuthRequestDTO.requestURI());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization failed");
        } else if (!tokenAuthRequestDTO.requestURI().startsWith("/component") && !"user".equals(jwtTokenType)) {
            logger.error("Extracted token type : {}, access to resource: {}  denied",
                    jwtTokenType, tokenAuthRequestDTO.requestURI());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization failed");
        }
        logger.info("Authorization successfully for token: {}, with type: {}, to resource: {}",
                tokenAuthRequestDTO.jwtToken(), jwtTokenType, tokenAuthRequestDTO.requestURI());
    }
}