package com.example.accountcomponent.config;

import com.example.accountcomponent.dto.AuthRequestDTO;
import com.example.accountcomponent.feign.AppRegistryComponentClient;
import com.example.accountcomponent.feign.SecurityComponentClient;
import com.example.accountcomponent.dto.AccountAppComponentConfigDTO;
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
public class ComponentConfigReader implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentConfigReader.class);
    private final AppRegistryComponentClient appRegistryComponentClient;
    private final SecurityComponentClient securityComponentClient;

    public ComponentConfigReader(@Qualifier("AppRegistry-Components") AppRegistryComponentClient appRegistryComponentClient,
                                 @Qualifier("Security-Components") SecurityComponentClient securityComponentClient) {
        this.appRegistryComponentClient = appRegistryComponentClient;
        this.securityComponentClient = securityComponentClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        LOGGER.debug("Trying to read and deserialize: account-component-config.yml file");
        AccountAppComponentConfigDTO accountConfig = readConfig();
        try {
            LOGGER.debug("Trying to register myself in: AppRegistry-Component");
            appRegistryComponentClient.registerComponent(accountConfig);
            LOGGER.debug("Component registered successfully: {},", accountConfig);

            LOGGER.debug("Trying to uthenticate myself in: Security-Component");
            AccountAppComponentConfigDTO.setJwtToken(securityComponentClient.authenticateComponent(
                    new AuthRequestDTO(accountConfig.getComponentId(), accountConfig.getComponentSecret())));
            LOGGER.debug("Authentication successfully, set up JWT Token: {}", AccountAppComponentConfigDTO.getJwtToken());
        } catch (FeignException feignResponseError) {
            LOGGER.error(feignResponseError.contentUTF8());
            cleanUp(accountConfig.getComponentId());
            System.exit(1);
        }
        LOGGER.debug("{} registered and authenticated successfully", accountConfig.getComponentName());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.debug("{} terminates, the shutdown hook is executed", accountConfig.getComponentName());
            cleanUp(accountConfig.getComponentId());
        }));
    }

    private void cleanUp(UUID componentId) {
        LOGGER.debug("Trying to deregister myself in: AppRegistry-Component");
        try {
            ResponseEntity<String> responseEntity = appRegistryComponentClient.deregisterComponent(componentId);
            LOGGER.debug(responseEntity.getBody());
        } catch (FeignException feignResponseError) {
            LOGGER.error(feignResponseError.contentUTF8());
        }
    }

    private static AccountAppComponentConfigDTO readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        AccountAppComponentConfigDTO accountConfig = null;
        try {
            accountConfig = objectMapper.readValue(new File(
                            "Account-Component/src/main/resources/account-component-config.yml"),
                    AccountAppComponentConfigDTO.class);
        } catch (IOException exception) {
            LOGGER.error("Error: File cannot be found or its contents cannot be deserialized");
            exception.printStackTrace();
        }
        LOGGER.debug("Deserialization successfully: {}", accountConfig);
        return accountConfig;
    }
}
