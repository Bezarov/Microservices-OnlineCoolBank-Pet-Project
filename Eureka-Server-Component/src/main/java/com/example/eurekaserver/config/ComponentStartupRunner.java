package com.example.eurekaserver.config;

import com.example.eurekaserver.dto.AuthResponseDTO;
import com.example.eurekaserver.dto.AuthRequestDTO;
import com.example.eurekaserver.feign.AppRegistryComponentClient;
import com.example.eurekaserver.feign.SecurityComponentClient;
import com.example.eurekaserver.filter.JwksProvider;
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
    private final EurekaServerAppComponentConfig eurekaServerConfig;
    private final AppRegistryComponentClient appRegistryComponentClient;
    private final SecurityComponentClient securityComponentClient;
    private final JwksProvider jwksProvider;

    public ComponentStartupRunner(EurekaServerAppComponentConfig eurekaServerConfig, @Qualifier("AppRegistry-Components") AppRegistryComponentClient appRegistryComponentClient,
                                  @Qualifier("Security-Components") SecurityComponentClient securityComponentClient, JwksProvider jwksProvider) {
        this.eurekaServerConfig = eurekaServerConfig;
        this.appRegistryComponentClient = appRegistryComponentClient;
        this.securityComponentClient = securityComponentClient;
        this.jwksProvider = jwksProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        EurekaServerAppComponentConfig configFromFile = readConfig();
        eurekaServerConfig.setComponentName(configFromFile.getComponentName());
        eurekaServerConfig.setComponentId(configFromFile.getComponentId());
        eurekaServerConfig.setComponentSecret(configFromFile.getComponentSecret());

        try {
            registerComponent(eurekaServerConfig);

            authenticateComponent(eurekaServerConfig);
            LOGGER.info("{} registered and authenticated successfully", eurekaServerConfig.getComponentName());
        } catch (FeignException feignException) {
            LOGGER.error(feignException.contentUTF8());
            cleanUp(eurekaServerConfig.getComponentId());
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("{} terminates, the shutdown hook is executed", eurekaServerConfig.getComponentName());
            cleanUp(eurekaServerConfig.getComponentId());
        }));
    }

    private EurekaServerAppComponentConfig readConfig() {
        LOGGER.info("Trying to read and deserialize: eureka-server-component-config.yml file");
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            return objectMapper.readValue(
                    new File("Eureka-Server-Component/src/main/resources/eureka-server-component-config.yml"),
                    EurekaServerAppComponentConfig.class);
        } catch (IOException exception) {
            LOGGER.error("Error: File cannot be found or its contents cannot be deserialized");
            throw new RuntimeException("Failed to load config file", exception);
        }
    }

    private void registerComponent(EurekaServerAppComponentConfig eurekaServerConfig) throws FeignException {
        LOGGER.info("Trying to register myself in: AppRegistry-Component");
        appRegistryComponentClient.registerComponent(eurekaServerConfig);
        LOGGER.info("Component registered successfully: {}", eurekaServerConfig);
    }

    private void authenticateComponent(EurekaServerAppComponentConfig eurekaServerConfig) throws FeignException {
        LOGGER.info("Trying to authenticate myself in: Security-Component");
        AuthResponseDTO authResponseDTO = securityComponentClient.authenticateComponent(new AuthRequestDTO(
                eurekaServerConfig.getComponentId(), eurekaServerConfig.getComponentSecret()));
        eurekaServerConfig.setJwtToken(authResponseDTO.token());
        jwksProvider.cacheInitKeys();
        LOGGER.info("Authentication successfully set up JWT Token: {}", eurekaServerConfig.getJwtToken());
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
