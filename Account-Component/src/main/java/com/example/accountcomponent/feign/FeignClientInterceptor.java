package com.example.accountcomponent.feign;

import com.example.accountcomponent.config.AccountAppComponentConfig;
import com.example.accountcomponent.dto.AuthResponseDTO;
import com.example.accountcomponent.dto.AuthRequestDTO;
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
    private final AccountAppComponentConfig accountConfig;

    public FeignClientInterceptor(@Qualifier("Security-Components") SecurityComponentClient securityComponentClient, AccountAppComponentConfig accountConfig) {
        this.securityComponentClient = securityComponentClient;
        this.accountConfig = accountConfig;
    }


    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            LOGGER.debug("Inject to feign request my JWT Token");
            requestTemplate.header("Authorization", "Bearer " + accountConfig.getJwtToken());
        } catch (FeignException.Unauthorized receivedFeignException) {
            if (receivedFeignException.status() == 401 &&
                    receivedFeignException.getMessage().contains("JWT token is expired, refresh it")) {
                LOGGER.warn("{} token expired, trying to refresh it", accountConfig.getJwtToken());
                refreshToken();
                apply(requestTemplate);
                LOGGER.debug("new Jwt Token set up successfully");
            }
        }
    }

    private void refreshToken() {
        try {
            AuthResponseDTO authResponseDTO = securityComponentClient.authenticateComponent(
                    new AuthRequestDTO(accountConfig.getComponentId(), accountConfig.getComponentSecret()));
            accountConfig.setJwtToken(authResponseDTO.token());
            LOGGER.debug("Token refreshed successfully: {}", accountConfig.getJwtToken());
        } catch (FeignException feignException) {
            LOGGER.error("Failed to refresh token: {}", feignException.contentUTF8());
            System.exit(1);
        }
    }
}