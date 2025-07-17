package com.example.paymentcomponent.feign;

import com.example.paymentcomponent.dto.AuthRequestDTO;
import com.example.paymentcomponent.dto.AuthResponseDTO;
import com.example.paymentcomponent.dto.JwksSpecificInfoDTO;
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
