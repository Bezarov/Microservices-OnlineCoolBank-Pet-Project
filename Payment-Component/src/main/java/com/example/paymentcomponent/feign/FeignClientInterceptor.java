package com.example.paymentcomponent.feign;

import com.example.paymentcomponent.dto.AuthRequestDTO;
import com.example.paymentcomponent.dto.AuthResponseDTO;
import com.example.paymentcomponent.config.PaymentAppComponentConfig;
import feign.FeignException;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeignClientInterceptor.class);
    private final SecurityComponentClient securityComponentClient;
    private final PaymentAppComponentConfig paymentConfig;

    public FeignClientInterceptor(@Qualifier("Security-Components") SecurityComponentClient securityComponentClient,
                                  PaymentAppComponentConfig paymentConfig) {
        this.securityComponentClient = securityComponentClient;
        this.paymentConfig = paymentConfig;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            LOGGER.debug("Inject to feign request my JWT Token");
            requestTemplate.header("Authorization", "Bearer " + paymentConfig.getJwtToken());
        } catch (FeignException.Unauthorized receivedFeignException) {
            if (receivedFeignException.status() == 401 &&
                    receivedFeignException.getMessage().contains("JWT token is expired, refresh it")) {
                LOGGER.warn("{} token expired, trying to refresh it", paymentConfig.getJwtToken());
                refreshToken();
                apply(requestTemplate);
                LOGGER.debug("new Jwt Token set up successfully");
            }
        }
    }

    private void refreshToken() {
        try {
            AuthResponseDTO authResponseDTO = securityComponentClient.authenticateComponent(
                    new AuthRequestDTO(paymentConfig.getComponentId(), paymentConfig.getComponentSecret()));
            paymentConfig.setJwtToken(authResponseDTO.token());
            LOGGER.debug("Token refreshed successfully: {}", paymentConfig.getJwtToken());
        } catch (FeignException feignException) {
            LOGGER.error("Failed to refresh token: {}", feignException.contentUTF8());
            System.exit(1);
        }
    }
}