package com.example.userscomponent.config;

import com.example.userscomponent.dto.AuthRequestDTO;
import com.example.userscomponent.dto.UsersAppComponentConfigDTO;
import com.example.userscomponent.feign.AppRegistryComponentClient;
import com.example.userscomponent.feign.SecurityComponentClient;
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
        logger.info("Trying to read and deserialize: users-component-config.yml file");
        UsersAppComponentConfigDTO usersConfig = readConfig();
        try {
            logger.info("Trying to register myself in: AppRegistry-Component");
            appRegistryComponentClient.registerComponent(usersConfig);
            logger.info("Component registered successfully: {}", usersConfig);
            logger.info("Trying to authenticate myself in: Security-Component");
            usersConfig.setToken(securityComponentClient.authenticateComponent(new AuthRequestDTO(
                    usersConfig.getComponentId(), usersConfig.getComponentSecret())));
            logger.info("Component authenticated successfully: {}", usersConfig.getToken());
        } catch (FeignException feignResponseError) {
            logger.error(feignResponseError.contentUTF8());
            System.exit(1);
        }
        logger.info("Users component registered and authenticated successfully");
    }

    public static UsersAppComponentConfigDTO readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        UsersAppComponentConfigDTO usersConfig = null;
        try {
            usersConfig = objectMapper.readValue(new File(
                    "src/main/resources/users-component-config.yml"), UsersAppComponentConfigDTO.class);
        } catch (IOException e) {
            logger.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        return usersConfig;
    }
}
