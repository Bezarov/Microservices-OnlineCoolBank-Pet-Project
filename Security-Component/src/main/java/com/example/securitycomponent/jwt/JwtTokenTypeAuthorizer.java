package com.example.securitycomponent.jwt;

import com.example.securitycomponent.dto.TokenAuthRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class JwtTokenTypeAuthorizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenTypeAuthorizer.class);
    private final JwtUtil jwtUtil;

    public JwtTokenTypeAuthorizer(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public void doTokenAuthorization(TokenAuthRequestDTO tokenAuthRequestDTO) {
        LOGGER.debug("Extracting token type");
        String jwtTokenType = jwtUtil.extractClaim(tokenAuthRequestDTO.jwtToken(), claims -> claims.get("tokenType", String.class));
        LOGGER.debug("Extracted token type is: \"{}\"", jwtTokenType);
        if (tokenAuthRequestDTO.requestURI().startsWith("/component") && !"component".equals(jwtTokenType)) {
            LOGGER.error("Extracted token type: \"{}\", access to resource: \"{}\"  denied",
                    jwtTokenType, tokenAuthRequestDTO.requestURI());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization failed");
        }
        LOGGER.info("Authorization successfully for token: \"{}\", with type: \"{}\", to resource: \"{}\"",
                tokenAuthRequestDTO.jwtToken(), jwtTokenType, tokenAuthRequestDTO.requestURI());
    }
}