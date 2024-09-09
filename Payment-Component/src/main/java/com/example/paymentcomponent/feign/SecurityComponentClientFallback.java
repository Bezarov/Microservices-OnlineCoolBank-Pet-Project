package com.example.paymentcomponent.feign;

import com.example.paymentcomponent.dto.AuthRequestDTO;
import com.example.paymentcomponent.dto.TokenAuthRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class SecurityComponentClientFallback implements SecurityComponentClient {
    @Override
    public String authenticateComponent(AuthRequestDTO authRequestDTO) {
        return null;
    }

    @Override
    public Boolean authenticateComponentToken(TokenAuthRequestDTO tokenAuthRequestDTO) {
        return null;
    }
}
