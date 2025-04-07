package com.example.securitycomponent.config;

import com.example.securitycomponent.dto.SecurityAppComponentConfigDTO;
import com.example.securitycomponent.feign.AppRegistryComponentClient;
import com.example.securitycomponent.jwt.JwtUtil;
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
    private SecurityAppComponentConfigDTO securityAppComponentConfigDTO;
    private final JwtUtil jwtUtil;

    public ComponentConfigReader(@Qualifier("AppRegistry-Components") AppRegistryComponentClient appRegistryComponentClient,
                                 SecurityAppComponentConfigDTO securityAppComponentConfigDTO, JwtUtil jwtUtil) {
        this.appRegistryComponentClient = appRegistryComponentClient;
        this.securityAppComponentConfigDTO = securityAppComponentConfigDTO;
        this.jwtUtil = jwtUtil;
    }


    @PostConstruct
    void init() {
        logger.info("Trying to read and deserialize: security-component-config.yml file");
        securityAppComponentConfigDTO = readConfig();
        logger.info("Deserialization successfully: {}", securityAppComponentConfigDTO);
        try {
            logger.info("Trying to authenticate myself");
            SecurityAppComponentConfigDTO.setJwtToken(jwtUtil.componentTokenGenerator(securityAppComponentConfigDTO.getComponentId().toString()));
            logger.info("Authentication successfully set up JWT Token: {}", SecurityAppComponentConfigDTO.getJwtToken());

            logger.info("Trying to register myself in: AppRegistry-Component");
            appRegistryComponentClient.registerComponent(securityAppComponentConfigDTO);
            logger.info("Component registered successfully: {}", securityAppComponentConfigDTO);
            logger.info("{} authenticated and registered successfully", securityAppComponentConfigDTO.getComponentName());
        } catch (FeignException feignResponseError) {
            logger.error(feignResponseError.contentUTF8());
            System.exit(1);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("{} terminates, the shutdown hook is executed", securityAppComponentConfigDTO.getComponentName());
            cleanUp(securityAppComponentConfigDTO.getComponentId());
        }));
    }

    public void cleanUp(UUID componentId) {
        logger.info("Trying to deregister myself in: AppRegistry-Component");
        try {
            ResponseEntity<String> responseEntity = appRegistryComponentClient.deregisterComponent(componentId);
            logger.info(responseEntity.getBody());
            System.exit(1);
        } catch (FeignException feignResponseError) {
            logger.error(feignResponseError.contentUTF8());
            System.exit(1);
        }
    }

    public static SecurityAppComponentConfigDTO readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        SecurityAppComponentConfigDTO securityConfig = null;
        try {
            securityConfig = objectMapper.readValue(new File(
                    "Security-Component/src/main/resources/security-component-config.yml"),
                    SecurityAppComponentConfigDTO.class);
        } catch (IOException e) {
            logger.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        return securityConfig;
    }
}