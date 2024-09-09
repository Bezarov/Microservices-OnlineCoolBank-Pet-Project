package com.example.apigatewaycomponent.feign;

import com.example.apigatewaycomponent.dto.ApiGatewayAppComponentConfigDTO;
import com.example.apigatewaycomponent.dto.AuthRequestDTO;
import feign.FeignException;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(FeignClientInterceptor.class);
    private final ApiGatewayAppComponentConfigDTO appComponentConfig;
    private final SecurityComponentClient securityComponentClient;

    public FeignClientInterceptor(ApiGatewayAppComponentConfigDTO appComponentConfig,
                                  @Qualifier("Security-Components") SecurityComponentClient securityComponentClient) {
        this.appComponentConfig = appComponentConfig;
        this.securityComponentClient = securityComponentClient;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            logger.info("Adding to feign request header Jwt Token");
            requestTemplate.header("Authorization", "Bearer " + appComponentConfig.getToken());
        } catch (FeignException.Unauthorized receivedFeignException) {
            if (receivedFeignException.status() == 401 &&
                    receivedFeignException.getMessage().contains("JWT token is expired, refresh it")) {
                logger.warn("{} token expired, trying to refresh it", appComponentConfig.getComponentName());
                refreshToken();
                logger.info("new Jwt Token set up successfully");
            }
        }
    }

    private void refreshToken() {
        try {
            String newToken = securityComponentClient.authenticateComponent(new AuthRequestDTO(
                    appComponentConfig.getComponentId(), appComponentConfig.getComponentSecret()));
            appComponentConfig.setToken(newToken);
            logger.info("Token refreshed successfully: {}", newToken);
        } catch (FeignException feignResponseError) {
            logger.error("Failed to refresh token: {}", feignResponseError.contentUTF8());
            System.exit(1);
        }
    }
}