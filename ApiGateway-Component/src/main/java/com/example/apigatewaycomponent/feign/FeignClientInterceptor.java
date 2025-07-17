package com.example.apigatewaycomponent.feign;

import com.example.apigatewaycomponent.config.ApiGatewayAppComponentConfig;
import com.example.apigatewaycomponent.dto.AuthRequestDTO;
import com.example.apigatewaycomponent.dto.AuthResponseDTO;
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
    private final ApiGatewayAppComponentConfig apiGatewayConfig;

    public FeignClientInterceptor(@Qualifier("Security-Components") SecurityComponentClient securityComponentClient, ApiGatewayAppComponentConfig apiGatewayConfig) {
        this.securityComponentClient = securityComponentClient;
        this.apiGatewayConfig = apiGatewayConfig;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            LOGGER.debug("Inject to feign request my JWT Token");
            requestTemplate.header("Authorization", "Bearer " + apiGatewayConfig.getJwtToken());
        } catch (FeignException.Unauthorized receivedFeignException) {
            if (receivedFeignException.status() == 401 &&
                    receivedFeignException.getMessage().contains("JWT token is expired, refresh it")) {
                LOGGER.warn("{} token expired, trying to refresh it", apiGatewayConfig.getJwtToken());
                refreshToken();
                apply(requestTemplate);
                LOGGER.debug("new Jwt Token set up successfully");
            }
        }
    }

    private void refreshToken() {
        try {
            AuthResponseDTO authResponseDTO = securityComponentClient.authenticateComponent(
                    new AuthRequestDTO(apiGatewayConfig.getComponentId(), apiGatewayConfig.getComponentSecret()));
            apiGatewayConfig.setJwtToken(authResponseDTO.token());
            LOGGER.debug("Token refreshed successfully: {}", apiGatewayConfig.getJwtToken());
        } catch (FeignException feignException) {
            LOGGER.error("Failed to refresh token: {}", feignException.contentUTF8());
            System.exit(1);
        }
    }
}