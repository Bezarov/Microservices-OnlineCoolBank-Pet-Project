package com.example.appregistrycomponent.feign;

import com.example.appregistrycomponent.dto.AuthRequestDTO;
import com.example.appregistrycomponent.dto.AuthResponseDTO;
import com.example.appregistrycomponent.dto.JwksSpecificInfoDTO;
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
