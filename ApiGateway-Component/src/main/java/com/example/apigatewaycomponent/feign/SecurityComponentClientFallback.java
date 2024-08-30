package com.example.apigatewaycomponent.feign;

import com.example.apigatewaycomponent.dto.AuthRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class SecurityComponentClientFallback implements SecurityComponentClient {

    @Override
    public String authenticateComponent(AuthRequestDTO authRequestDTO) {
        return null;
    }
}
