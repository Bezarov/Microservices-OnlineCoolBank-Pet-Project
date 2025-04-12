package com.example.securitycomponent.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static SecretKey userSecretKey;
    private static SecretKey componentSecretKey;
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String USER_TOKEN_TYPE = "user";
    private static final String COMPONENT_TOKEN_TYPE = "component";
    private static String userSecretKeyBase64 = "WakandaBank";
    private static String componentSecretKeyBase64 = "BankWakanda";
    private static final String SALT = "WAKANDAWAKANDAWAKANDAAAABANK!";

    @PostConstruct
    public static void init() {
        userSecretKeyBase64 = userSecretKeyBase64 + SALT;
        componentSecretKeyBase64 = componentSecretKeyBase64 + SALT;

        userSecretKey = Keys.hmacShaKeyFor(Base64.getEncoder().encode(userSecretKeyBase64.getBytes()));
        componentSecretKey = Keys.hmacShaKeyFor(Base64.getEncoder().encode(componentSecretKeyBase64.getBytes()));
        logger.debug("Initial keys for user and component was successfully encrypted and assigned");
    }

    public String userTokenGenerator(String userEmail) {
        Map<String, Object> claims = new HashMap<>();
        logger.debug("Setting token type as users");
        claims.put(TOKEN_TYPE_CLAIM, USER_TOKEN_TYPE);
        logger.debug("Generating token");
        return doGenerateToken(claims, userEmail, userSecretKey);
    }

    public String componentTokenGenerator(String serviceId) {
        Map<String, Object> claims = new HashMap<>();
        logger.debug("Setting token type as component");
        claims.put(TOKEN_TYPE_CLAIM, COMPONENT_TOKEN_TYPE);
        logger.debug("Generating token");
        return doGenerateToken(claims, serviceId, componentSecretKey);
    }

    private String doGenerateToken(Map<String, Object> claims, String subject, SecretKey secretKey) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                //10 Hours
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(secretKey)
                .compact();
    }

    public String getIdentityFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        SecretKey key = determineKeyForToken(token);
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    private SecretKey determineKeyForToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(userSecretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException exception) {
            claims = Jwts.parserBuilder()
                    .setSigningKey(componentSecretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }

        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        if (COMPONENT_TOKEN_TYPE.equals(tokenType)) {
            return componentSecretKey;
        } else {
            return userSecretKey;
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateUserToken(String token) {
        return (isUserToken(token) && !isTokenExpired(token));
    }

    public Boolean validateComponentToken(String token) {
        return (isComponentToken(token) && !isTokenExpired(token));
    }

    public Boolean isUserToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(userSecretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return USER_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (SignatureException exception) {
            return false;
        }
    }

    public Boolean isComponentToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(componentSecretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return COMPONENT_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (SignatureException exception) {
            return false;
        }
    }
}
