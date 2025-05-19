package com.example.paymentcomponent.config;

import com.example.paymentcomponent.dto.AuthRequestDTO;
import com.example.paymentcomponent.dto.PaymentAppComponentConfigDTO;
import com.example.paymentcomponent.feign.AppRegistryComponentClient;
import com.example.paymentcomponent.feign.SecurityComponentClient;
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
        LOGGER.info("Trying to read and deserialize: payment-component-config.yml file");
        PaymentAppComponentConfigDTO paymentConfig = readConfig();
        LOGGER.info("Deserialization successfully: {}", paymentConfig);
        try {
            LOGGER.info("Trying to register myself in: AppRegistry-Component");
            appRegistryComponentClient.registerComponent(paymentConfig);
            LOGGER.info("Component registered successfully: {}", paymentConfig);
            LOGGER.info("Trying to authenticate myself in: Security-Component");
            PaymentAppComponentConfigDTO.setJwtToken(securityComponentClient.authenticateComponent(new AuthRequestDTO(
                    paymentConfig.getComponentId(), paymentConfig.getComponentSecret())));
            LOGGER.info("Authentication successfully set up JWT Token: {}", PaymentAppComponentConfigDTO.getJwtToken());
        } catch (FeignException feignResponseError) {
            LOGGER.error(feignResponseError.contentUTF8());
            cleanUp(paymentConfig.getComponentId());
            System.exit(1);
        }
        LOGGER.info("{} registered and authenticated successfully", paymentConfig.getComponentName());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("{} terminates, the shutdown hook is executed", paymentConfig.getComponentName());
            cleanUp(paymentConfig.getComponentId());
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

    private static PaymentAppComponentConfigDTO readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        PaymentAppComponentConfigDTO paymentConfig = null;
        try {
            paymentConfig = objectMapper.readValue(new File(
                            "Payment-Component/src/main/resources/payment-component-config.yml"),
                    PaymentAppComponentConfigDTO.class);
        } catch (IOException e) {
            LOGGER.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        return paymentConfig;
    }
}
