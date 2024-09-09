package com.example.securitycomponent.feign;

import com.example.securitycomponent.dto.SecurityAppComponentConfigDTO;
import feign.FeignException;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(FeignClientInterceptor.class);

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            logger.debug("Adding to feign request in Authentication header my JWT Token: {}",
                    SecurityAppComponentConfigDTO.getJwtToken());
            requestTemplate.header("Authorization", "Bearer " + SecurityAppComponentConfigDTO.getJwtToken());
        } catch (FeignException.Unauthorized receivedFeignException) {
            if (receivedFeignException.status() == 401 &&
                    receivedFeignException.getMessage().contains("JWT token is expired, refresh it")) {
                logger.warn("{} token expired, trying to refresh it", SecurityAppComponentConfigDTO.getJwtToken());
                logger.debug("new Jwt Token set up successfully");
            }
        }
    }
}