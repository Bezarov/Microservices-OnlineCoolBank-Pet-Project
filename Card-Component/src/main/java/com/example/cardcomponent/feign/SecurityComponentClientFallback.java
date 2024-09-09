package com.example.cardcomponent.feign;

import com.example.cardcomponent.dto.AuthRequestDTO;
import com.example.cardcomponent.dto.TokenAuthRequestDTO;
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
