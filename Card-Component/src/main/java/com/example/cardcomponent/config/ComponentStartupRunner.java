package com.example.cardcomponent.config;

import com.example.cardcomponent.dto.AuthRequestDTO;
import com.example.cardcomponent.dto.AuthResponseDTO;
import com.example.cardcomponent.feign.AppRegistryComponentClient;
import com.example.cardcomponent.feign.SecurityComponentClient;
import com.example.cardcomponent.filter.JwksProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class ComponentStartupRunner implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentStartupRunner.class);
    private final CardAppComponentConfig cardConfig;
    private final AppRegistryComponentClient appRegistryComponentClient;
    private final SecurityComponentClient securityComponentClient;
    private final JwksProvider jwksProvider;

    public ComponentStartupRunner(CardAppComponentConfig cardConfig, @Qualifier("AppRegistry-Components") AppRegistryComponentClient appRegistryComponentClient,
                                  @Qualifier("Security-Components") SecurityComponentClient securityComponentClient, JwksProvider jwksProvider) {
        this.cardConfig = cardConfig;
        this.appRegistryComponentClient = appRegistryComponentClient;
        this.securityComponentClient = securityComponentClient;
        this.jwksProvider = jwksProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        CardAppComponentConfig configFromFile = readConfig();
        cardConfig.setComponentName(configFromFile.getComponentName());
        cardConfig.setComponentId(configFromFile.getComponentId());
        cardConfig.setComponentSecret(configFromFile.getComponentSecret());

        try {
            registerComponent(cardConfig);

            authenticateComponent(cardConfig);
            LOGGER.info("{} registered and authenticated successfully", cardConfig.getComponentName());
        } catch (FeignException feignException) {
            LOGGER.error(feignException.contentUTF8());
            cleanUp(cardConfig.getComponentId());
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("{} terminates, the shutdown hook is executed", cardConfig.getComponentName());
            cleanUp(cardConfig.getComponentId());
        }));
    }

    private CardAppComponentConfig readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            return objectMapper.readValue(
                    new File("Card-Component/src/main/resources/card-component-config.yml"),
                    CardAppComponentConfig.class);
        } catch (IOException exception) {
            LOGGER.error("Error: File cannot be found or its contents cannot be deserialized");
            throw new RuntimeException("Failed to load config file", exception);
        }
    }

    private void registerComponent(CardAppComponentConfig configConfig) throws FeignException {
        LOGGER.info("Trying to register myself in: AppRegistry-Component");
        appRegistryComponentClient.registerComponent(configConfig);
        LOGGER.info("Component registered successfully: {}", configConfig);
    }

    private void authenticateComponent(CardAppComponentConfig cardConfig) throws FeignException {
        LOGGER.info("Trying to authenticate myself in: Security-Component");
        AuthResponseDTO authResponseDTO = securityComponentClient.authenticateComponent(new AuthRequestDTO(
                cardConfig.getComponentId(), cardConfig.getComponentSecret()));
        cardConfig.setJwtToken(authResponseDTO.token());
        jwksProvider.cacheInitKeys();
        LOGGER.info("Authentication successfully set up JWT Token: {}", cardConfig.getJwtToken());
    }

    private void cleanUp(UUID componentId) {
        LOGGER.info("Trying to deregister myself in: AppRegistry-Component");
        try {
            ResponseEntity<String> responseEntity = appRegistryComponentClient.deregisterComponent(componentId);
            LOGGER.info(responseEntity.getBody());
        } catch (FeignException feignResponseError) {
            LOGGER.error(feignResponseError.contentUTF8());
        }
    }
}
