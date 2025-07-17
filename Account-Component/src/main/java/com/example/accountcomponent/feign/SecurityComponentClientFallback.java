package com.example.accountcomponent.feign;

import com.example.accountcomponent.dto.AuthResponseDTO;
import com.example.accountcomponent.dto.AuthRequestDTO;
import com.example.accountcomponent.dto.JwksSpecificInfoDTO;
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
