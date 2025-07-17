package com.example.userscomponent.feign;

import com.example.userscomponent.dto.AuthResponseDTO;
import com.example.userscomponent.dto.AuthRequestDTO;
import com.example.userscomponent.config.UsersAppComponentConfig;
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
    private final UsersAppComponentConfig usersConfig;

    public FeignClientInterceptor(@Qualifier("Security-Components") SecurityComponentClient securityComponentClient,
                                  UsersAppComponentConfig usersConfig) {
        this.securityComponentClient = securityComponentClient;
        this.usersConfig = usersConfig;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            LOGGER.debug("Inject to feign request my JWT Token");
            requestTemplate.header("Authorization", "Bearer " + usersConfig.getJwtToken());
        } catch (FeignException.Unauthorized receivedFeignException) {
            if (receivedFeignException.status() == 401 &&
                    receivedFeignException.getMessage().contains("JWT token is expired, refresh it")) {
                LOGGER.warn("{} token expired, trying to refresh it", usersConfig.getJwtToken());
                refreshToken();
                apply(requestTemplate);
                LOGGER.debug("new Jwt Token set up successfully");
            }
        }
    }

    private void refreshToken() {
        try {
            AuthResponseDTO authResponseDTO = securityComponentClient.authenticateComponent(
                    new AuthRequestDTO(usersConfig.getComponentId(), usersConfig.getComponentSecret()));
            usersConfig.setJwtToken(authResponseDTO.token());
            LOGGER.debug("Token refreshed successfully: {}", usersConfig.getJwtToken());
        } catch (FeignException feignException) {
            LOGGER.error("Failed to refresh token: {}", feignException.contentUTF8());
            System.exit(1);
        }
    }
}