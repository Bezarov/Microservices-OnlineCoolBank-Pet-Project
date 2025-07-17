package com.example.securitycomponent.utils;

import com.example.securitycomponent.dto.JwksSpecificInfoDTO;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);
    private static final String USER_KID = "user-key";
    private static final String COMPONENT_KID = "component-key";

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String USER_TOKEN_TYPE = "user";
    private static final String COMPONENT_TOKEN_TYPE = "component";

    private KeyPair userKeyPair;
    private KeyPair componentKeyPair;

    @PostConstruct
    public void init() {
        userKeyPair = Jwts.SIG.RS256.keyPair().build();
        componentKeyPair = Jwts.SIG.RS256.keyPair().build();
        LOGGER.info("RSA key pair (RS256) for user & component tokens generated ({}-bit)", 2048);
    }

    public String generateUserToken(String userEmail) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, USER_TOKEN_TYPE);
        return buildToken(claims, userEmail, userKeyPair.getPrivate(), USER_KID);
    }

    public String generateComponentToken(String serviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, COMPONENT_TOKEN_TYPE);
        return buildToken(claims, serviceId, componentKeyPair.getPrivate(), COMPONENT_KID);
    }

    private String buildToken(Map<String, Object> claims, String subject, PrivateKey privateKey, String kid) {
        return Jwts.builder()
                .header().type("JWT").add("kid", kid).and()
                .claims()
                .add(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .and()
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public JwksSpecificInfoDTO getJwksSpecificInfo() {
        List<JwksSpecificInfoDTO.JwksKeyDTO> keys = List.of(
                buildJwk(USER_KID, userKeyPair.getPublic()),
                buildJwk(COMPONENT_KID, componentKeyPair.getPublic())
        );
        return new JwksSpecificInfoDTO(keys);
    }

    private JwksSpecificInfoDTO.JwksKeyDTO buildJwk(String kid, PublicKey publicKey) {
        try {
            java.security.interfaces.RSAPublicKey rsaPublicKey = (java.security.interfaces.RSAPublicKey) publicKey;

            String n = Base64.getUrlEncoder().withoutPadding().encodeToString(rsaPublicKey.getModulus().toByteArray());
            String e = Base64.getUrlEncoder().withoutPadding().encodeToString(rsaPublicKey.getPublicExponent().toByteArray());

            return new JwksSpecificInfoDTO.JwksKeyDTO("RSA", kid, "RS256", n, e);
        } catch (ClassCastException exception) {
            throw new IllegalStateException("PublicKey is not an RSA key", exception);
        }
    }
}
