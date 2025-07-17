package com.example.accountcomponent.config;

import com.example.accountcomponent.dto.AuthResponseDTO;
import com.example.accountcomponent.dto.AuthRequestDTO;
import com.example.accountcomponent.feign.AppRegistryComponentClient;
import com.example.accountcomponent.feign.SecurityComponentClient;
import com.example.accountcomponent.filter.JwksProvider;
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
    private final AccountAppComponentConfig accountConfig;
    private final AppRegistryComponentClient appRegistryComponentClient;
    private final SecurityComponentClient securityComponentClient;
    private final JwksProvider jwksProvider;

    public ComponentStartupRunner(AccountAppComponentConfig accountConfig, @Qualifier("AppRegistry-Components") AppRegistryComponentClient appRegistryComponentClient,
                                  @Qualifier("Security-Components") SecurityComponentClient securityComponentClient, JwksProvider jwksProvider) {
        this.accountConfig = accountConfig;
        this.appRegistryComponentClient = appRegistryComponentClient;
        this.securityComponentClient = securityComponentClient;
        this.jwksProvider = jwksProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        AccountAppComponentConfig configFromFile = readConfig();
        accountConfig.setComponentName(configFromFile.getComponentName());
        accountConfig.setComponentId(configFromFile.getComponentId());
        accountConfig.setComponentSecret(configFromFile.getComponentSecret());

        try {
            registerComponent(accountConfig);

            authenticateComponent(accountConfig);
            LOGGER.info("{} registered and authenticated successfully", accountConfig.getComponentName());
        } catch (FeignException feignException) {
            LOGGER.error(feignException.contentUTF8());
            cleanUp(accountConfig.getComponentId());
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.debug("{} terminates, the shutdown hook is executed", accountConfig.getComponentName());
            cleanUp(accountConfig.getComponentId());
        }));
    }

    private AccountAppComponentConfig readConfig() {
        LOGGER.info("Trying to read and deserialize: account-component-config.yml file");
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            return objectMapper.readValue(
                    new File("Account-Component/src/main/resources/account-component-config.yml"),
                    AccountAppComponentConfig.class);
        } catch (IOException exception) {
            LOGGER.error("Error: File cannot be found or its contents cannot be deserialized");
            throw new RuntimeException("Failed to load config file", exception);
        }
    }

    private void registerComponent(AccountAppComponentConfig accountConfig) throws FeignException {
        LOGGER.info("Trying to register myself in: AppRegistry-Component");
        appRegistryComponentClient.registerComponent(accountConfig);
        LOGGER.info("Component registered successfully: {}", accountConfig);
    }

    private void authenticateComponent(AccountAppComponentConfig accountConfig) throws FeignException {
        LOGGER.info("Trying to authenticate myself in: Security-Component");
        AuthResponseDTO authResponseDTO = securityComponentClient.authenticateComponent(
                new AuthRequestDTO(accountConfig.getComponentId(), accountConfig.getComponentSecret()));
        accountConfig.setJwtToken(authResponseDTO.token());
        jwksProvider.cacheInitKeys();
        LOGGER.info("Authentication successfully set up JWT Token: {}", accountConfig.getJwtToken());
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
}
