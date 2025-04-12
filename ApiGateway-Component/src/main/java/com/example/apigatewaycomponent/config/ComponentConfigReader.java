package com.example.apigatewaycomponent.config;

import com.example.apigatewaycomponent.dto.ApiGatewayAppComponentConfigDTO;
import com.example.apigatewaycomponent.dto.AuthRequestDTO;
import com.example.apigatewaycomponent.feign.AppRegistryComponentClient;
import com.example.apigatewaycomponent.feign.SecurityComponentClient;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentConfigReader.class);
    private final AppRegistryComponentClient appRegistryComponentClient;
    private final SecurityComponentClient securityComponentClient;

    public ComponentConfigReader(@Qualifier("AppRegistry-Components") AppRegistryComponentClient appRegistryComponentClient,
                                 @Qualifier("Security-Components") SecurityComponentClient securityComponentClient) {
        this.appRegistryComponentClient = appRegistryComponentClient;
        this.securityComponentClient = securityComponentClient;
    }

    @PostConstruct
    void init() {
        LOGGER.info("Trying to read and deserialize: apigateway-component-config.yml file");
        ApiGatewayAppComponentConfigDTO apiGatewayConfig = readConfig();
        LOGGER.info("Deserialization successfully: {}", apiGatewayConfig);
        try {
            LOGGER.info("Trying to register myself in: AppRegistry-Component");
            appRegistryComponentClient.registerComponent(apiGatewayConfig);
            LOGGER.info("Component registered successfully: {}", apiGatewayConfig);
            LOGGER.info("Trying to authenticate myself in: Security-Component");
            ApiGatewayAppComponentConfigDTO.setJwtToken(securityComponentClient.authenticateComponent(new AuthRequestDTO(
                    apiGatewayConfig.getComponentId(), apiGatewayConfig.getComponentSecret())));
            LOGGER.info("Authentication successfully set up JWT Token: {}", ApiGatewayAppComponentConfigDTO.getJwtToken());
        } catch (FeignException feignResponseError) {
            LOGGER.error(feignResponseError.contentUTF8());
            cleanUp(apiGatewayConfig.getComponentId());
            System.exit(1);
        }
        LOGGER.info("{} registered and authenticated successfully", apiGatewayConfig.getComponentName());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("{} terminates, the shutdown hook is executed", apiGatewayConfig.getComponentName());
            cleanUp(apiGatewayConfig.getComponentId());
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

    private static ApiGatewayAppComponentConfigDTO readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        ApiGatewayAppComponentConfigDTO apiGatewayConfig = null;
        try {
            apiGatewayConfig = objectMapper.readValue(new File(
                    "ApiGateway-Component/src/main/resources/apigateway-component-config.yml"),
                    ApiGatewayAppComponentConfigDTO.class);
        } catch (IOException e) {
            LOGGER.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        return apiGatewayConfig;
    }
}

