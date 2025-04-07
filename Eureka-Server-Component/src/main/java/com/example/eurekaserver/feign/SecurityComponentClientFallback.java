package com.example.eurekaserver.feign;

import com.example.eurekaserver.dto.AuthRequestDTO;
import com.example.eurekaserver.dto.TokenAuthRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class SecurityComponentClientFallback implements SecurityComponentClient{
    @Override
    public String authenticateComponent(AuthRequestDTO authRequestDTO) {
        return null;
    }

    @Override
    public Boolean authenticateComponentToken(TokenAuthRequestDTO tokenAuthRequestDTO) {
        return null;
    }
}
