package com.example.securitycomponent.feign;

import com.example.securitycomponent.dto.SecurityAppComponentConfigDTO;
import com.example.securitycomponent.jwt.JwtUtil;
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
    private final SecurityAppComponentConfigDTO securityAppComponentConfigDTO;

    public FeignClientInterceptor(JwtUtil jwtUtil, SecurityAppComponentConfigDTO securityAppComponentConfigDTO) {
        this.jwtUtil = jwtUtil;
        this.securityAppComponentConfigDTO = securityAppComponentConfigDTO;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            LOGGER.debug("Inject to feign request my JWT Token");
            requestTemplate.header("Authorization", "Bearer " + SecurityAppComponentConfigDTO.getJwtToken());
        } catch (FeignException.Unauthorized receivedFeignException) {
            if (receivedFeignException.status() == 401 &&
                    receivedFeignException.getMessage().contains("JWT token is expired, refresh it")) {
                LOGGER.warn("{} token expired, trying to refresh it", SecurityAppComponentConfigDTO.getJwtToken());
                SecurityAppComponentConfigDTO.setJwtToken(jwtUtil.componentTokenGenerator(
                        securityAppComponentConfigDTO.getComponentId().toString()));
                LOGGER.debug("new Jwt Token set up successfully");
            }
        }
    }
}