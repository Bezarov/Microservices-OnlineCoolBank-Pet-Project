package com.example.securitycomponent.config;

import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.dto.SecurityAppComponentConfigDTO;
import com.example.securitycomponent.feign.AppRegistryComponentClient;
import com.example.securitycomponent.service.AuthService;
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
    private final AuthService authService;

    public ComponentConfigReader(@Qualifier("AppRegistry-Components") AppRegistryComponentClient appRegistryComponentClient,
                                 AuthService authService) {
        this.appRegistryComponentClient = appRegistryComponentClient;
        this.authService = authService;
    }

    @PostConstruct
    void init() {
        logger.info("Trying to read and deserialize: security-component-config.yml file");
        SecurityAppComponentConfigDTO securityConfig = readConfig();
        logger.info("Deserialization successfully: {}", securityConfig);
        try {
            logger.info("Trying to register myself in: AppRegistry-Component");
            appRegistryComponentClient.registerComponent(securityConfig);
            logger.info("Component registered successfully: {}", securityConfig);
            logger.info("Trying to authenticate myself in: Security-Component");

            securityConfig.setToken(authService.authenticateComponent(
                    new AuthRequestDTO(securityConfig.getComponentId(), securityConfig.getComponentSecret())
            ));
            logger.info("Component authenticated successfully: {}", securityConfig.getToken());
        } catch (FeignException feignResponseError) {
            logger.error(feignResponseError.contentUTF8());
            System.exit(1);
        }
        logger.info("Security component registered and authenticated successfully");
    }

    public static SecurityAppComponentConfigDTO readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        SecurityAppComponentConfigDTO securityConfig = null;
        try {
            securityConfig = objectMapper.readValue(new File(
                    "src/main/resources/security-component-config.yml"), SecurityAppComponentConfigDTO.class);
        } catch (IOException e) {
            logger.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        return securityConfig;
    }
}