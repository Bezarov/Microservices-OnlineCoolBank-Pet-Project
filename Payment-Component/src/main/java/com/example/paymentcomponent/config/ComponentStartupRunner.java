package com.example.paymentcomponent.config;

import com.example.paymentcomponent.dto.AuthRequestDTO;
import com.example.paymentcomponent.dto.AuthResponseDTO;
import com.example.paymentcomponent.feign.AppRegistryComponentClient;
import com.example.paymentcomponent.feign.SecurityComponentClient;
import com.example.paymentcomponent.filter.JwksProvider;
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
    private final PaymentAppComponentConfig paymentConfig;
    private final AppRegistryComponentClient appRegistryComponentClient;
    private final SecurityComponentClient securityComponentClient;
    private final JwksProvider jwksProvider;

    public ComponentStartupRunner(PaymentAppComponentConfig paymentConfig, @Qualifier("AppRegistry-Components") AppRegistryComponentClient appRegistryComponentClient,
                                  @Qualifier("Security-Components") SecurityComponentClient securityComponentClient, JwksProvider jwksProvider) {
        this.paymentConfig = paymentConfig;
        this.appRegistryComponentClient = appRegistryComponentClient;
        this.securityComponentClient = securityComponentClient;
        this.jwksProvider = jwksProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        PaymentAppComponentConfig configFromFile = readConfig();
        paymentConfig.setComponentName(configFromFile.getComponentName());
        paymentConfig.setComponentId(configFromFile.getComponentId());
        paymentConfig.setComponentSecret(configFromFile.getComponentSecret());

        try {
            registerComponent(paymentConfig);

            authenticateComponent(paymentConfig);
            LOGGER.info("{} registered and authenticated successfully", paymentConfig.getComponentName());
        } catch (FeignException feignException) {
            LOGGER.error(feignException.contentUTF8());
            cleanUp(paymentConfig.getComponentId());
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("{} terminates, the shutdown hook is executed", paymentConfig.getComponentName());
            cleanUp(paymentConfig.getComponentId());
        }));
    }

    private PaymentAppComponentConfig readConfig() {
        LOGGER.info("Trying to read and deserialize: payment-component-config.yml file");
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            return objectMapper.readValue(
                    new File("Payment-Component/src/main/resources/payment-component-config.yml"),
                    PaymentAppComponentConfig.class);
        } catch (IOException exception) {
            LOGGER.error("Error: File cannot be found or its contents cannot be deserialized");
            throw new RuntimeException("Failed to load config file", exception);
        }
    }

    private void registerComponent(PaymentAppComponentConfig paymentConfig) throws FeignException {
        LOGGER.info("Trying to register myself in: AppRegistry-Component");
        appRegistryComponentClient.registerComponent(paymentConfig);
        LOGGER.info("Component registered successfully: {}", paymentConfig);
    }

    private void authenticateComponent(PaymentAppComponentConfig paymentConfig) throws FeignException {
        LOGGER.info("Trying to authenticate myself in: Security-Component");
        AuthResponseDTO authResponseDTO = securityComponentClient.authenticateComponent(new AuthRequestDTO(
                paymentConfig.getComponentId(), paymentConfig.getComponentSecret()));
        paymentConfig.setJwtToken(authResponseDTO.token());
        jwksProvider.cacheInitKeys();
        LOGGER.info("Authentication successfully set up JWT Token: {}", paymentConfig.getJwtToken());
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
