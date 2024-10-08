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
    private final SecurityComponentClient securityComponentClient;
    private final ApiGatewayAppComponentConfigDTO appComponentConfigDTO;

    public FeignClientInterceptor(@Qualifier("Security-Components") SecurityComponentClient securityComponentClient, ApiGatewayAppComponentConfigDTO appComponentConfigDTO) {
        this.securityComponentClient = securityComponentClient;
        this.appComponentConfigDTO = appComponentConfigDTO;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            logger.debug("Adding to feign request header my JWT Token");
            requestTemplate.header("Authorization", "Bearer " + ApiGatewayAppComponentConfigDTO.getJwtToken());
        } catch (FeignException.Unauthorized receivedFeignException) {
            if (receivedFeignException.status() == 401 &&
                    receivedFeignException.getMessage().contains("JWT token is expired, refresh it")) {
                logger.warn("{} token expired, trying to refresh it", ApiGatewayAppComponentConfigDTO.getJwtToken());
                refreshToken();
                logger.debug("new Jwt Token set up successfully");
            }
        }
    }

    private void refreshToken() {
        try {
            String newToken = securityComponentClient.authenticateComponent(new AuthRequestDTO(
                    appComponentConfigDTO.getComponentId(), appComponentConfigDTO.getComponentSecret()));
            ApiGatewayAppComponentConfigDTO.setJwtToken(newToken);
            logger.debug("Token refreshed successfully: {}", newToken);
        } catch (FeignException feignResponseError) {
            logger.error("Failed to refresh token: {}", feignResponseError.contentUTF8());
            System.exit(1);
        }
    }
}