package com.example.accountcomponent.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtVerifier.class);

    private final JwksProvider jwksProvider;

    public JwtVerifier(JwksProvider jwksProvider) {
        this.jwksProvider = jwksProvider;
    }

    public String verify(String jwt) throws AuthenticationException {
        Claims claims = parseClaims(jwt);
        ensureNotExpired(claims);
        return claims.getSubject();
    }

    private Claims parseClaims(String jwt) throws InsufficientAuthenticationException {
        try {
            return Jwts.parser()
                    .keyLocator(keyLocator())
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        } catch (Exception exception) {
            LOGGER.warn(exception.getMessage());
            throw new InsufficientAuthenticationException(
                    "Unable to verify JWT Token, please get an acceptable JWT Token, and try again");
        }
    }

    private Locator<Key> keyLocator() throws InsufficientAuthenticationException {
        return header -> {
            JwsHeader jwsHeader = (JwsHeader) header;
            String kid = jwsHeader.getKeyId();
            return jwksProvider.getKeyByKid(kid).orElseThrow(() -> {
                LOGGER.warn("Unknown kid: {} -> key can't be found for JWT Header", ((JwsHeader) header).getKeyId());
                throw new InsufficientAuthenticationException("Unknown kid: " + kid + "key was not found in key cache");
            });
        };
    }

    private void ensureNotExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration != null && expiration.before(new Date())) {
            LOGGER.debug("JWT token for subject was expired: {}", claims.getSubject());
            throw new InsufficientAuthenticationException("JWT token is expired, refresh it");
        }
    }
}