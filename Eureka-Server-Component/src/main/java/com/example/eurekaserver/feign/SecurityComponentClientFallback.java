package com.example.eurekaserver.feign;

import com.example.eurekaserver.dto.AuthResponseDTO;
import com.example.eurekaserver.dto.AuthRequestDTO;
import com.example.eurekaserver.dto.JwksSpecificInfoDTO;
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
