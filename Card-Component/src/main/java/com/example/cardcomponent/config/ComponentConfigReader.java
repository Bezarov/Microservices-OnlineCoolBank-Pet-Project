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
    private static final Logger logger = LoggerFactory.getLogger(ComponentConfigReader.class);
    private final AppRegistryComponentClient appRegistryComponentClient;
    private final SecurityComponentClient securityComponentClient;

    public ComponentConfigReader(@Qualifier("AppRegistry-Components") AppRegistryComponentClient appRegistryComponentClient,
                                 @Qualifier("Security-Components") SecurityComponentClient securityComponentClient) {
        this.appRegistryComponentClient = appRegistryComponentClient;
        this.securityComponentClient = securityComponentClient;
    }

    @PostConstruct
    void init() {
        logger.info("Trying to read and deserialize: card-component-config.yml file");
        CardAppComponentConfigDTO cardConfig = readConfig();
        logger.info("Deserialization successfully: {}", cardConfig);
        try {
            logger.info("Trying to register myself in: AppRegistry-Component");
            appRegistryComponentClient.registerComponent(cardConfig);
            logger.info("Component registered successfully: {}", cardConfig);
            logger.info("Trying to authenticate myself in: Security-Component");
            CardAppComponentConfigDTO.setJwtToken(securityComponentClient.authenticateComponent(new AuthRequestDTO(
                    cardConfig.getComponentId(), cardConfig.getComponentSecret())));
            logger.info("Authentication successfully set up JWT Token: {}", CardAppComponentConfigDTO.getJwtToken());
        } catch (FeignException feignResponseError) {
            logger.error(feignResponseError.contentUTF8());
            cleanUp(cardConfig.getComponentId());
            System.exit(1);
        }
        logger.info("{} registered and authenticated successfully", cardConfig.getComponentName());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("{} terminates, the shutdown hook is executed", cardConfig.getComponentName());
            cleanUp(cardConfig.getComponentId());
        }));
    }

    private void cleanUp(UUID componentId) {
        logger.info("Trying to deregister myself in: AppRegistry-Component");
        try {
            ResponseEntity<String> responseEntity = appRegistryComponentClient.deregisterComponent(componentId);
            logger.info(responseEntity.getBody());
        } catch (FeignException feignResponseError) {
            logger.error(feignResponseError.contentUTF8());
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
            logger.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        return cardConfig;
    }
}
