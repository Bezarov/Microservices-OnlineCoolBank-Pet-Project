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
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

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
        logger.info("Trying to read and deserialize: apigateway-component-config.yml file");
        ApiGatewayAppComponentConfigDTO apiGatewayConfig = readConfig();
        try {
            logger.info("Trying to register myself in: AppRegistry-Component");
            appRegistryComponentClient.registerComponent(apiGatewayConfig);
            logger.info("Component registered successfully: {}", apiGatewayConfig);
            logger.info("Trying to authenticate myself in: Security-Component");
            apiGatewayConfig.setToken(securityComponentClient.authenticateComponent(new AuthRequestDTO(
                    apiGatewayConfig.getComponentId(), apiGatewayConfig.getComponentSecret())));
            logger.info("Component authenticated successfully: {}", apiGatewayConfig.getToken());
        } catch (FeignException feignResponseError) {
            logger.error(feignResponseError.contentUTF8());
            System.exit(1);
        }
        logger.info("ApiGateway component registered and authenticated successfully");
    }

    public static ApiGatewayAppComponentConfigDTO readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        ApiGatewayAppComponentConfigDTO apiGatewayConfig = null;
        try {
            apiGatewayConfig = objectMapper.readValue(new File(
                    "src/main/resources/apigateway-component-config.yml"), ApiGatewayAppComponentConfigDTO.class);
        } catch (IOException e) {
            logger.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        return apiGatewayConfig;
    }
}

