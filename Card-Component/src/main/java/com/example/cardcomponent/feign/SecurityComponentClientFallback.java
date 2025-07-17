package com.example.cardcomponent.feign;

import com.example.cardcomponent.dto.AuthRequestDTO;
import com.example.cardcomponent.dto.AuthResponseDTO;
import com.example.cardcomponent.dto.JwksSpecificInfoDTO;
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
