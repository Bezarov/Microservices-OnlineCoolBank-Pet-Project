package com.example.securitycomponent.config;

import com.example.securitycomponent.feign.AppRegistryComponentClient;
import com.example.securitycomponent.utils.JwtUtil;
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
    private final SecurityAppComponentConfig securityConfig;
    private final AppRegistryComponentClient appRegistryComponentClient;
    private final JwtUtil jwtUtil;

    public ComponentStartupRunner(SecurityAppComponentConfig securityConfig, @Qualifier("AppRegistry-Components") AppRegistryComponentClient appRegistryComponentClient, JwtUtil jwtUtil) {
        this.securityConfig = securityConfig;
        this.appRegistryComponentClient = appRegistryComponentClient;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void run(ApplicationArguments args) {
        SecurityAppComponentConfig configFromFile = readConfig();
        securityConfig.setComponentName(configFromFile.getComponentName());
        securityConfig.setComponentId(configFromFile.getComponentId());
        securityConfig.setComponentSecret(configFromFile.getComponentSecret());

        try {
            registerComponent(securityConfig);

            securityConfig.setJwtToken(jwtUtil.generateComponentToken(
                    securityConfig.getComponentId().toString()));
            LOGGER.info("Authentication successfully set up JWT Token: {}", securityConfig.getJwtToken());

            LOGGER.info("{} authenticated and registered successfully", securityConfig.getComponentName());
        } catch (FeignException feignResponseError) {
            LOGGER.error(feignResponseError.contentUTF8());
            cleanUp(securityConfig.getComponentId());
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("{} terminates, the shutdown hook is executed", securityConfig.getComponentName());
            cleanUp(securityConfig.getComponentId());
        }));
    }

    private SecurityAppComponentConfig readConfig() {
        LOGGER.info("Trying to read and deserialize: security-component-config.yml file");
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            return objectMapper.readValue(
                    new File("Security-Component/src/main/resources/security-component-config.yml"),
                    SecurityAppComponentConfig.class);
        } catch (IOException exception) {
            LOGGER.error("Error: File cannot be found or its contents cannot be deserialized");
            throw new RuntimeException("Failed to load config file", exception);
        }
    }

    private void registerComponent(SecurityAppComponentConfig securityConfig) throws FeignException {
        LOGGER.info("Trying to register myself in: AppRegistry-Component");
        appRegistryComponentClient.registerComponent(securityConfig);
        LOGGER.info("Component registered successfully: {}", securityConfig);
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