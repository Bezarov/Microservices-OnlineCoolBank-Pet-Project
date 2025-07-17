package com.example.securitycomponent.feign;

import com.example.securitycomponent.config.SecurityAppComponentConfig;
import com.example.securitycomponent.utils.JwtUtil;
import feign.FeignException;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeignClientInterceptor.class);
    private final JwtUtil jwtUtil;
    private final SecurityAppComponentConfig securityConfig;

    public FeignClientInterceptor(JwtUtil jwtUtil, SecurityAppComponentConfig securityConfig) {
        this.jwtUtil = jwtUtil;
        this.securityConfig = securityConfig;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String method = requestTemplate.method();
        String path = requestTemplate.path();

        if ("GET".equalsIgnoreCase(method) && path.matches(".*/by-id/[^/]+$")) {
            LOGGER.debug("Skipping JWT injection for: {}", path);
            return;
        }

        try {
            LOGGER.debug("Inject to feign request my JWT Token");
            requestTemplate.header("Authorization", "Bearer " + securityConfig.getJwtToken());
        } catch (FeignException.Unauthorized receivedFeignException) {
            if (receivedFeignException.status() == 401 &&
                    receivedFeignException.getMessage().contains("JWT token is expired, refresh it")) {
                LOGGER.warn("{} token expired, trying to refresh it", securityConfig.getJwtToken());
                refreshToken();
                apply(requestTemplate);
                LOGGER.debug("new Jwt Token set up successfully");
            }
        }
    }

    private void refreshToken() {
        try {
            securityConfig.setJwtToken(jwtUtil.generateComponentToken(
                    securityConfig.getComponentId().toString()));
            LOGGER.debug("Token refreshed successfully: {}", securityConfig.getJwtToken());
        } catch (Exception exception) {
            LOGGER.error("Failed to refresh token: {}", exception.getMessage());
            System.exit(1);
        }
    }
}