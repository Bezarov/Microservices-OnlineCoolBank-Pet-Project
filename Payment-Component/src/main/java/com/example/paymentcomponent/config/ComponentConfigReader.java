package com.example.paymentcomponent.config;

import com.example.paymentcomponent.dto.AuthRequestDTO;
import com.example.paymentcomponent.dto.PaymentAppComponentConfigDTO;
import com.example.paymentcomponent.feign.AppRegistryComponentClient;
import com.example.paymentcomponent.feign.SecurityComponentClient;
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
        logger.info("Trying to read and deserialize: payment-component-config.yml file");
        PaymentAppComponentConfigDTO paymentConfig = readConfig();
        try {
            logger.info("Trying to register myself in: AppRegistry-Component");
            appRegistryComponentClient.registerComponent(paymentConfig);
            logger.info("Component registered successfully: {}", paymentConfig);
            logger.info("Trying to authenticate myself in: Security-Component");
            paymentConfig.setToken(securityComponentClient.authenticateComponent(new AuthRequestDTO(
                    paymentConfig.getComponentId(), paymentConfig.getComponentSecret())));
            logger.info("Component authenticated successfully: {}", paymentConfig.getToken());
        } catch (FeignException feignResponseError) {
            logger.error(feignResponseError.contentUTF8());
            System.exit(1);
        }
        logger.info("Payment component registered and authenticated successfully");
    }

    public static PaymentAppComponentConfigDTO readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        PaymentAppComponentConfigDTO paymentConfig = null;
        try {
            paymentConfig = objectMapper.readValue(new File(
                    "src/main/resources/payment-component-config.yml"), PaymentAppComponentConfigDTO.class);
        } catch (IOException e) {
            logger.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        return paymentConfig;
    }
}
