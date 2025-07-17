package com.example.accountcomponent.filter;

import com.example.accountcomponent.config.AccountAppComponentConfig;
import com.example.accountcomponent.dto.AuthRequestDTO;
import com.example.accountcomponent.dto.JwksSpecificInfoDTO;
import com.example.accountcomponent.feign.SecurityComponentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwksProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwksProvider.class);
    private final Map<String, PublicKey> jwkCache = new ConcurrentHashMap<>();
    private final SecurityComponentClient securityComponentClient;
    private final AccountAppComponentConfig accountConfig;

    public JwksProvider(@Qualifier("Security-Components") SecurityComponentClient securityComponentClient, AccountAppComponentConfig accountConfig) {
        this.securityComponentClient = securityComponentClient;
        this.accountConfig = accountConfig;
    }

    public void cacheInitKeys() {
        LOGGER.info("Initializing JWKS public keys from Security-Component on startup");
        JwksSpecificInfoDTO jwks = securityComponentClient.getActualJwks(
                new AuthRequestDTO(accountConfig.getComponentId(), accountConfig.getComponentSecret()));

        Map<String, PublicKey> initialKeys = new HashMap<>();

        for (JwksSpecificInfoDTO.JwksKeyDTO jwk : jwks.getKeys()) {
            buildRsaPublicKey(jwk.getN(), jwk.getE())
                    .ifPresentOrElse(
                            key -> initialKeys.put(jwk.getKid(), key),
                            () -> LOGGER.warn("Skipping key with kid={} it couldn't be built", jwk.getKid()));
        }

        jwkCache.clear();
        jwkCache.putAll(initialKeys);
        LOGGER.info("JWKS initialization completed. Loaded keys: {}", initialKeys.keySet());
    }

    @Scheduled(initialDelay = 5 * 60 * 1000, fixedDelay = 5 * 60 * 1000)
    private void updateKeys() {
        LOGGER.debug("Scheduled Public keys update started");
        JwksSpecificInfoDTO jwks = securityComponentClient.getActualJwks(
                new AuthRequestDTO(accountConfig.getComponentId(), accountConfig.getComponentSecret()));

        Map<String, PublicKey> updatedKeys = new HashMap<>();

        for (JwksSpecificInfoDTO.JwksKeyDTO jwk : jwks.getKeys()) {
            buildRsaPublicKey(jwk.getN(), jwk.getE())
                    .ifPresentOrElse(
                            key -> updatedKeys.put(jwk.getKid(), key),
                            () -> LOGGER.warn("Skipping key with kid={} it couldn't be built", jwk.getKid()));
        }

        jwkCache.clear();
        jwkCache.putAll(updatedKeys);
        LOGGER.debug("Scheduled Public keys update successfully ended");
    }

    protected Optional<PublicKey> getKeyByKid(String kid) {
        return Optional.ofNullable(jwkCache.get(kid));
    }

    private Optional<PublicKey> buildRsaPublicKey(String n, String e) {
        try {
            byte[] modulusBytes = Base64.getUrlDecoder().decode(n);
            byte[] exponentBytes = Base64.getUrlDecoder().decode(e);

            BigInteger modulus = new BigInteger(1, modulusBytes);
            BigInteger exponent = new BigInteger(1, exponentBytes);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return Optional.of(keyFactory.generatePublic(spec));

        } catch (InvalidKeySpecException | NoSuchAlgorithmException exception) {
            LOGGER.error("Failed to build public key: {}", exception.getMessage());
            return Optional.empty();
        }
    }
}
