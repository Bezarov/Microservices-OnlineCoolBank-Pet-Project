package com.example.accountcomponent.feign;

import com.example.accountcomponent.dto.AccountAppComponentConfigDTO;
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
    private static final Logger logger = LoggerFactory.getLogger(FeignClientInterceptor.class);
    private final SecurityComponentClient securityComponentClient;
    private final AccountAppComponentConfigDTO appComponentConfigDTO;

    public FeignClientInterceptor(@Qualifier("Security-Components") SecurityComponentClient securityComponentClient,
                                  AccountAppComponentConfigDTO appComponentConfigDTO) {
        this.securityComponentClient = securityComponentClient;
        this.appComponentConfigDTO = appComponentConfigDTO;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            logger.info("Adding to feign request header Jwt my Token");
            requestTemplate.header("Authorization", "Bearer " + AccountAppComponentConfigDTO.getJwtToken());
        } catch (FeignException.Unauthorized receivedFeignException) {
            if (receivedFeignException.status() == 401 &&
                    receivedFeignException.getMessage().contains("JWT token is expired, refresh it")) {
                logger.warn("{} token expired, trying to refresh it", AccountAppComponentConfigDTO.getJwtToken());
                refreshToken();
                logger.info("new Jwt Token set up successfully");
            }
        }
    }

    private void refreshToken() {
        try {
            String newToken = securityComponentClient.authenticateComponent(new AuthRequestDTO(
                    appComponentConfigDTO.getComponentId(), appComponentConfigDTO.getComponentSecret()));
            AccountAppComponentConfigDTO.setJwtToken(newToken);
            logger.info("Token refreshed successfully: {}", newToken);
        } catch (FeignException feignResponseError) {
            logger.error("Failed to refresh token: {}", feignResponseError.contentUTF8());
            System.exit(1);
        }
    }
}