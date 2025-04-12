package com.example.cardcomponent.config;

import com.example.cardcomponent.dto.AuthRequestDTO;
import com.example.cardcomponent.dto.CardAppComponentConfigDTO;
import com.example.cardcomponent.feign.AppRegistryComponentClient;
import com.example.cardcomponent.feign.SecurityComponentClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import feign.FeignException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class ComponentConfigReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentConfigReader.class);
    private final AppRegistryComponentClient appRegistryComponentClient;
    private final SecurityComponentClient securityComponentClient;

    public ComponentConfigReader(@Qualifier("AppRegistry-Components") AppRegistryComponentClient appRegistryComponentClient,
                                 @Qualifier("Security-Components") SecurityComponentClient securityComponentClient) {
        this.appRegistryComponentClient = appRegistryComponentClient;
        this.securityComponentClient = securityComponentClient;
    }

    @PostConstruct
    void init() {
        LOGGER.info("Trying to read and deserialize: card-component-config.yml file");
        CardAppComponentConfigDTO cardConfig = readConfig();
        LOGGER.info("Deserialization successfully: {}", cardConfig);
        try {
            LOGGER.info("Trying to register myself in: AppRegistry-Component");
            appRegistryComponentClient.registerComponent(cardConfig);
            LOGGER.info("Component registered successfully: {}", cardConfig);
            LOGGER.info("Trying to authenticate myself in: Security-Component");
            CardAppComponentConfigDTO.setJwtToken(securityComponentClient.authenticateComponent(new AuthRequestDTO(
                    cardConfig.getComponentId(), cardConfig.getComponentSecret())));
            LOGGER.info("Authentication successfully set up JWT Token: {}", CardAppComponentConfigDTO.getJwtToken());
        } catch (FeignException feignResponseError) {
            LOGGER.error(feignResponseError.contentUTF8());
            cleanUp(cardConfig.getComponentId());
            System.exit(1);
        }
        LOGGER.info("{} registered and authenticated successfully", cardConfig.getComponentName());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("{} terminates, the shutdown hook is executed", cardConfig.getComponentName());
            cleanUp(cardConfig.getComponentId());
        }));
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

    private static CardAppComponentConfigDTO readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        CardAppComponentConfigDTO cardConfig = null;
        try {
            cardConfig = objectMapper.readValue(new File(
                    "Card-Component/src/main/resources/card-component-config.yml"),
                    CardAppComponentConfigDTO.class);
        } catch (IOException e) {
            LOGGER.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        return cardConfig;
    }
}
