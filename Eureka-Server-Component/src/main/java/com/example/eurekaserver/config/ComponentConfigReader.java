package com.example.eurekaserver.config;

import com.example.eurekaserver.dto.AuthRequestDTO;
import com.example.eurekaserver.dto.EurekaServerAppComponentDTO;
import com.example.eurekaserver.feign.AppRegistryComponentClient;
import com.example.eurekaserver.feign.SecurityComponentClient;
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
        LOGGER.info("Trying to read and deserialize: eureka-server-component-config.yml file");
        EurekaServerAppComponentDTO eurekaServerConfig = readConfig();
        try {
            LOGGER.info("Trying to register myself in: AppRegistry-Component");
            appRegistryComponentClient.registerComponent(eurekaServerConfig);
            LOGGER.info("Component registered successfully: {}", eurekaServerConfig);
            LOGGER.info("Trying to authenticate myself in: Security-Component");
            EurekaServerAppComponentDTO.setJwtToken(securityComponentClient.authenticateComponent(new AuthRequestDTO(
                    eurekaServerConfig.getComponentId(), eurekaServerConfig.getComponentSecret())));
            LOGGER.info("Authentication successfully set up JWT Token: {}", EurekaServerAppComponentDTO.getJwtToken());
        } catch (FeignException feignResponseError) {
            LOGGER.error(feignResponseError.contentUTF8());
            cleanUp(eurekaServerConfig.getComponentId());
            System.exit(1);
        }
        LOGGER.info("{} registered and authenticated successfully", eurekaServerConfig.getComponentName());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("{} terminates, the shutdown hook is executed", eurekaServerConfig.getComponentName());
            cleanUp(eurekaServerConfig.getComponentId());
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

    private static EurekaServerAppComponentDTO readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        EurekaServerAppComponentDTO eurekaServerConfig = null;
        try {
            eurekaServerConfig = objectMapper.readValue(new File(
                    "Eureka-Server-Component/src/main/resources/eureka-server-component-config.yml"),
                    EurekaServerAppComponentDTO.class);
        } catch (IOException e) {
            LOGGER.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        return eurekaServerConfig;
    }
}
