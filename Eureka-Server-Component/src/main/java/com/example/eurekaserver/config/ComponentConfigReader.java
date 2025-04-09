package com.example.eurekaserver.config;

import com.example.eurekaserver.dto.AuthRequestDTO;
import com.example.eurekaserver.dto.EurekaServerAppComponentDTO;
import com.example.eurekaserver.feign.AppRegistryComponentClient;
import com.example.eurekaserver.feign.SecurityComponentClient;
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
        logger.info("Trying to read and deserialize: eureka-server-component-config.yml file");
        EurekaServerAppComponentDTO eurekaServerConfig = readConfig();
        try {
            logger.info("Trying to register myself in: AppRegistry-Component");
            appRegistryComponentClient.registerComponent(eurekaServerConfig);
            logger.info("Component registered successfully: {}", eurekaServerConfig);
            logger.info("Trying to authenticate myself in: Security-Component");
            EurekaServerAppComponentDTO.setJwtToken(securityComponentClient.authenticateComponent(new AuthRequestDTO(
                    eurekaServerConfig.getComponentId(), eurekaServerConfig.getComponentSecret())));
            logger.info("Authentication successfully set up JWT Token: {}", EurekaServerAppComponentDTO.getJwtToken());
        } catch (FeignException feignResponseError) {
            logger.error(feignResponseError.contentUTF8());
            cleanUp(eurekaServerConfig.getComponentId());
            System.exit(1);
        }
        logger.info("{} registered and authenticated successfully", eurekaServerConfig.getComponentName());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("{} terminates, the shutdown hook is executed", eurekaServerConfig.getComponentName());
            cleanUp(eurekaServerConfig.getComponentId());
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

    private static EurekaServerAppComponentDTO readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        EurekaServerAppComponentDTO eurekaServerConfig = null;
        try {
            eurekaServerConfig = objectMapper.readValue(new File(
                    "Eureka-Server-Component/src/main/resources/eureka-server-component-config.yml"),
                    EurekaServerAppComponentDTO.class);
        } catch (IOException e) {
            logger.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        return eurekaServerConfig;
    }
}
