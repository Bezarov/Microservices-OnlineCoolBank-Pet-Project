package com.example.userscomponent.config;

import com.example.userscomponent.dto.AuthResponseDTO;
import com.example.userscomponent.dto.AuthRequestDTO;
import com.example.userscomponent.feign.AppRegistryComponentClient;
import com.example.userscomponent.feign.SecurityComponentClient;
import com.example.userscomponent.filter.JwksProvider;
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
    private final UsersAppComponentConfig usersConfig;
    private final AppRegistryComponentClient appRegistryComponentClient;
    private final SecurityComponentClient securityComponentClient;
    private final JwksProvider jwksProvider;

    public ComponentStartupRunner(UsersAppComponentConfig usersConfig, @Qualifier("AppRegistry-Components") AppRegistryComponentClient appRegistryComponentClient,
                                  @Qualifier("Security-Components") SecurityComponentClient securityComponentClient, JwksProvider jwksProvider) {
        this.usersConfig = usersConfig;
        this.appRegistryComponentClient = appRegistryComponentClient;
        this.securityComponentClient = securityComponentClient;
        this.jwksProvider = jwksProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        UsersAppComponentConfig configFromFile = readConfig();
        usersConfig.setComponentName(configFromFile.getComponentName());
        usersConfig.setComponentId(configFromFile.getComponentId());
        usersConfig.setComponentSecret(configFromFile.getComponentSecret());

        try {
            registerComponent(usersConfig);

            authenticateComponent(usersConfig);
            LOGGER.info("{} registered and authenticated successfully", usersConfig.getComponentName());
        } catch (FeignException feignException) {
            LOGGER.error(feignException.contentUTF8());
            cleanUp(usersConfig.getComponentId());
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("{} terminates, the shutdown hook is executed", usersConfig.getComponentName());
            cleanUp(usersConfig.getComponentId());
        }));
    }

    private UsersAppComponentConfig readConfig() {
        LOGGER.info("Trying to read and deserialize: users-component-config.yml file");
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            return objectMapper.readValue(
                    new File("Users-Component/src/main/resources/users-component-config.yml"),
                    UsersAppComponentConfig.class);
        } catch (IOException exception) {
            LOGGER.error("Error: File cannot be found or its contents cannot be deserialized");
            throw new RuntimeException("Failed to load config file", exception);
        }
    }

    private void registerComponent(UsersAppComponentConfig usersConfig) throws FeignException {
        LOGGER.info("Trying to register myself in: AppRegistry-Component");
        appRegistryComponentClient.registerComponent(usersConfig);
        LOGGER.info("Component registered successfully: {}", usersConfig);
    }

    private void authenticateComponent(UsersAppComponentConfig usersConfig) throws FeignException {
        LOGGER.info("Trying to authenticate myself in: Security-Component");
        AuthResponseDTO authResponseDTO = securityComponentClient.authenticateComponent(new AuthRequestDTO(
                usersConfig.getComponentId(), usersConfig.getComponentSecret()));
        usersConfig.setJwtToken(authResponseDTO.token());
        jwksProvider.cacheInitKeys();
        LOGGER.info("Authentication successfully set up JWT Token: {}", usersConfig.getJwtToken());
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
