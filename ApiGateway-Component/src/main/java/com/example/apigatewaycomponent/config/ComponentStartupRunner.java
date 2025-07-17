package com.example.apigatewaycomponent.config;

import com.example.apigatewaycomponent.dto.AuthRequestDTO;
import com.example.apigatewaycomponent.dto.AuthResponseDTO;
import com.example.apigatewaycomponent.feign.AppRegistryComponentClient;
import com.example.apigatewaycomponent.feign.SecurityComponentClient;
import com.example.apigatewaycomponent.filter.JwksProvider;
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
    private final ApiGatewayAppComponentConfig apiGatewayConfig;
    private final AppRegistryComponentClient appRegistryComponentClient;
    private final SecurityComponentClient securityComponentClient;
    private final JwksProvider jwksProvider;

    public ComponentStartupRunner(ApiGatewayAppComponentConfig apiGatewayConfig, @Qualifier("AppRegistry-Components") AppRegistryComponentClient appRegistryComponentClient,
                                  @Qualifier("Security-Components") SecurityComponentClient securityComponentClient, JwksProvider jwksProvider) {
        this.apiGatewayConfig = apiGatewayConfig;
        this.appRegistryComponentClient = appRegistryComponentClient;
        this.securityComponentClient = securityComponentClient;
        this.jwksProvider = jwksProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        ApiGatewayAppComponentConfig configFromFile = readConfig();
        apiGatewayConfig.setComponentName(configFromFile.getComponentName());
        apiGatewayConfig.setComponentId(configFromFile.getComponentId());
        apiGatewayConfig.setComponentSecret(configFromFile.getComponentSecret());

        try {
            registerComponent(apiGatewayConfig);

            authenticateComponent(apiGatewayConfig);
            LOGGER.info("{} registered and authenticated successfully", apiGatewayConfig.getComponentName());
        } catch (FeignException feignException) {
            LOGGER.error(feignException.contentUTF8());
            cleanUp(apiGatewayConfig.getComponentId());
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("{} terminates, the shutdown hook is executed", apiGatewayConfig.getComponentName());
            cleanUp(apiGatewayConfig.getComponentId());
        }));
    }

    private ApiGatewayAppComponentConfig readConfig() {
        LOGGER.info("Trying to read and deserialize: apigateway-component-config.yml file");
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            return objectMapper.readValue(
                    new File("ApiGateway-Component/src/main/resources/apigateway-component-config.yml"),
                    ApiGatewayAppComponentConfig.class);
        } catch (IOException exception) {
            LOGGER.error("Error: File cannot be found or its contents cannot be deserialized");
            throw new RuntimeException("Failed to load config file", exception);
        }
    }

    private void registerComponent(ApiGatewayAppComponentConfig apiGatewayConfig) throws FeignException {
        LOGGER.info("Trying to register myself in: AppRegistry-Component");
        appRegistryComponentClient.registerComponent(apiGatewayConfig);
        LOGGER.info("Component registered successfully: {}", apiGatewayConfig);
    }

    private void authenticateComponent(ApiGatewayAppComponentConfig apiGatewayConfig) throws FeignException {
        LOGGER.info("Trying to authenticate myself in: Security-Component");
        AuthResponseDTO authResponseDTO = securityComponentClient.authenticateComponent(new AuthRequestDTO(
                apiGatewayConfig.getComponentId(), apiGatewayConfig.getComponentSecret()));
        apiGatewayConfig.setJwtToken(authResponseDTO.token());
        jwksProvider.cacheInitKeys();
        LOGGER.info("Authentication successfully set up JWT Token: {}", apiGatewayConfig.getJwtToken());
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

