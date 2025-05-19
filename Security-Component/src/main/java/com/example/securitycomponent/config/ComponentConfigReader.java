package com.example.securitycomponent.config;

import com.example.securitycomponent.dto.SecurityAppComponentConfigDTO;
import com.example.securitycomponent.feign.AppRegistryComponentClient;
import com.example.securitycomponent.jwt.JwtUtil;
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
    private SecurityAppComponentConfigDTO securityAppComponentConfigDTO;
    private final JwtUtil jwtUtil;

    public ComponentConfigReader(@Qualifier("AppRegistry-Components") AppRegistryComponentClient appRegistryComponentClient,
                                 SecurityAppComponentConfigDTO securityAppComponentConfigDTO, JwtUtil jwtUtil) {
        this.appRegistryComponentClient = appRegistryComponentClient;
        this.securityAppComponentConfigDTO = securityAppComponentConfigDTO;
        this.jwtUtil = jwtUtil;
    }


    @Override
    public void run(ApplicationArguments args) {
        LOGGER.info("Trying to read and deserialize: security-component-config.yml file");
        securityAppComponentConfigDTO = readConfig();
        LOGGER.info("Deserialization successfully: {}", securityAppComponentConfigDTO);
        try {
            LOGGER.info("Trying to authenticate myself");
            SecurityAppComponentConfigDTO.setJwtToken(jwtUtil.componentTokenGenerator(
                    securityAppComponentConfigDTO.getComponentId().toString()));
            LOGGER.info("Authentication successfully set up JWT Token: {}", SecurityAppComponentConfigDTO.getJwtToken());

            LOGGER.info("Trying to register myself in: AppRegistry-Component");
            appRegistryComponentClient.registerComponent(securityAppComponentConfigDTO);
            LOGGER.info("Component registered successfully: {}", securityAppComponentConfigDTO);
            LOGGER.info("{} authenticated and registered successfully", securityAppComponentConfigDTO.getComponentName());
        } catch (FeignException feignResponseError) {
            LOGGER.error(feignResponseError.contentUTF8());
            cleanUp(securityAppComponentConfigDTO.getComponentId());
            System.exit(1);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("{} terminates, the shutdown hook is executed", securityAppComponentConfigDTO.getComponentName());
            cleanUp(securityAppComponentConfigDTO.getComponentId());
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

    private static SecurityAppComponentConfigDTO readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        SecurityAppComponentConfigDTO securityConfig = null;
        try {
            securityConfig = objectMapper.readValue(new File(
                    "Security-Component/src/main/resources/security-component-config.yml"),
                    SecurityAppComponentConfigDTO.class);
        } catch (IOException e) {
            LOGGER.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        return securityConfig;
    }
}