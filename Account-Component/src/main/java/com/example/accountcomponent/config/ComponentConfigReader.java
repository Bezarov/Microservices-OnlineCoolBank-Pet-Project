package com.example.accountcomponent.config;

import com.example.accountcomponent.dto.AuthRequestDTO;
import com.example.accountcomponent.feign.AppRegistryComponentClient;
import com.example.accountcomponent.feign.SecurityComponentClient;
import com.example.accountcomponent.dto.AccountAppComponentConfigDTO;
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
        logger.info("Trying to read and deserialize: account-component-config.yml file");
        AccountAppComponentConfigDTO accountConfig = readConfig();
        logger.info("Deserialization successfully: {}", accountConfig);
        try {
            logger.info("Trying to register myself in: AppRegistry-Component");
            appRegistryComponentClient.registerComponent(accountConfig);
            logger.info("Component registered successfully: {}", accountConfig);
            logger.info("Trying to authenticate myself in: Security-Component");

            accountConfig.setToken(securityComponentClient.authenticateComponent(new AuthRequestDTO(
                    accountConfig.getComponentId(), accountConfig.getComponentSecret())));
            logger.info("Component authenticated successfully: {}", accountConfig.getToken());
        } catch (FeignException feignResponseError) {
            logger.error(feignResponseError.contentUTF8());
            System.exit(1);
        }
        logger.info("Account component registered and authenticated successfully");
    }

    public static AccountAppComponentConfigDTO readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        AccountAppComponentConfigDTO accountConfig = null;
        try {
            accountConfig = objectMapper.readValue(new File(
                    "src/main/resources/account-component-config.yml"), AccountAppComponentConfigDTO.class);
        } catch (IOException e) {
            logger.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        return accountConfig;
    }
}
