package com.example.eurekaserver.feign;

import com.example.eurekaserver.dto.AuthRequestDTO;
import com.example.eurekaserver.dto.EurekaServerAppComponentDTO;
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
    private final EurekaServerAppComponentDTO appComponentConfigDTO;

    public FeignClientInterceptor(@Qualifier("Security-Components") SecurityComponentClient securityComponentClient,
                                  EurekaServerAppComponentDTO appComponentConfigDTO) {
        this.securityComponentClient = securityComponentClient;
        this.appComponentConfigDTO = appComponentConfigDTO;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            LOGGER.debug("Inject to feign request my JWT Token");
            requestTemplate.header("Authorization", "Bearer " + EurekaServerAppComponentDTO.getJwtToken());
        } catch (FeignException.Unauthorized receivedFeignException) {
            if (receivedFeignException.status() == 401 &&
                    receivedFeignException.getMessage().contains("JWT token is expired, refresh it")) {
                LOGGER.warn("{} token expired, trying to refresh it", EurekaServerAppComponentDTO.getJwtToken());
                refreshToken();
                LOGGER.debug("new Jwt Token set up successfully");
            }
        }
    }

    private void refreshToken() {
        try {
            EurekaServerAppComponentDTO.setJwtToken(securityComponentClient.authenticateComponent(new AuthRequestDTO(
                    appComponentConfigDTO.getComponentId(), appComponentConfigDTO.getComponentSecret())));
            LOGGER.debug("Token refreshed successfully: {}", EurekaServerAppComponentDTO.getJwtToken());
        } catch (FeignException feignResponseError) {
            LOGGER.error("Failed to refresh token: {}", feignResponseError.contentUTF8());
            System.exit(1);
        }
    }
}