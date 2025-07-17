package com.example.apigatewaycomponent.feign;

import com.example.apigatewaycomponent.dto.AuthRequestDTO;
import com.example.apigatewaycomponent.dto.AuthResponseDTO;
import com.example.apigatewaycomponent.dto.JwksSpecificInfoDTO;
import org.springframework.stereotype.Component;

@Component
public class SecurityComponentClientFallback implements SecurityComponentClient {

    @Override
    public AuthResponseDTO authenticateComponent(AuthRequestDTO authRequestDTO) {
        return null;
    }

    @Override
    public JwksSpecificInfoDTO getActualJwks(AuthRequestDTO authRequestDTO) {
        return null;
    }
}
