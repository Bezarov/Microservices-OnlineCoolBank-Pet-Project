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
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

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
        try {
            logger.info("Trying to register myself in: AppRegistry-Component");
            appRegistryComponentClient.registerComponent(cardConfig);
            logger.info("Component registered successfully: {}", cardConfig);
            logger.info("Trying to authenticate myself in: Security-Component");
            cardConfig.setToken(securityComponentClient.authenticateComponent(new AuthRequestDTO(
                    cardConfig.getComponentId(), cardConfig.getComponentSecret())));
            logger.info("Component authenticated successfully: {}", cardConfig.getToken());
        } catch (FeignException feignResponseError) {
            logger.error(feignResponseError.contentUTF8());
            System.exit(1);
        }
        logger.info("Card component registered and authenticated successfully");
    }

    public static CardAppComponentConfigDTO readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        CardAppComponentConfigDTO cardConfig = null;
        try {
            cardConfig = objectMapper.readValue(new File(
                    "src/main/resources/card-component-config.yml"), CardAppComponentConfigDTO.class);
        } catch (IOException e) {
            logger.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        return cardConfig;
    }
}
