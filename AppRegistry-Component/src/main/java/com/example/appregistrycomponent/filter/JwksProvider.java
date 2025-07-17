package com.example.appregistrycomponent.filter;

import com.example.appregistrycomponent.config.AppRegistryComponentConfig;
import com.example.appregistrycomponent.dto.AuthRequestDTO;
import com.example.appregistrycomponent.dto.AuthResponseDTO;
import com.example.appregistrycomponent.dto.JwksSpecificInfoDTO;
import com.example.appregistrycomponent.feign.SecurityComponentClient;
import feign.FeignException;
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
    private final AppRegistryComponentConfig appRegistryConfig;
    private final SecurityComponentClient securityComponentClient;

    public JwksProvider(AppRegistryComponentConfig appRegistryConfig, @Qualifier("Security-Components") SecurityComponentClient securityComponentClient) {
        this.appRegistryConfig = appRegistryConfig;
        this.securityComponentClient = securityComponentClient;
    }


    public void cacheInitKeys() {
        LOGGER.info("Initializing JWKS public keys from Security-Component on startup");
        authenticateComponent();
        JwksSpecificInfoDTO jwks = getJwks();

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
        Map<String, PublicKey> updatedKeys = new HashMap<>();
        JwksSpecificInfoDTO jwks = getJwks();

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

    public Optional<PublicKey> getKeyByKid(String kid) {
        if (jwkCache.isEmpty())
            cacheInitKeys();
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

    private void authenticateComponent() throws FeignException {
        LOGGER.info("Trying to authenticate myself in: Security-Component");
        AuthResponseDTO authResponseDTO = securityComponentClient.authenticateComponent(new AuthRequestDTO(
                appRegistryConfig.getComponentId(), appRegistryConfig.getComponentSecret()));
        appRegistryConfig.setJwtToken(authResponseDTO.token());
        LOGGER.info("Authentication successfully set up JWT Token: {}", appRegistryConfig.getJwtToken());
    }

    private JwksSpecificInfoDTO getJwks() throws FeignException {
        return securityComponentClient.getActualJwks(
                new AuthRequestDTO(appRegistryConfig.getComponentId(), appRegistryConfig.getComponentSecret()));
    }
}
