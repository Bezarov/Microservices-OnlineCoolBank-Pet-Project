package com.example.eurekaserver.feign;

import com.example.eurekaserver.dto.AuthRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class SecurityComponentClientFallback implements SecurityComponentClient{
    @Override
    public String authenticateComponent(AuthRequestDTO authRequestDTO) {
        return null;
    }
}
