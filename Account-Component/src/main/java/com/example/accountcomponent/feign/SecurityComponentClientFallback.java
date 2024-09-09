package com.example.accountcomponent.feign;

import com.example.accountcomponent.dto.AuthRequestDTO;
import com.example.accountcomponent.dto.TokenAuthRequestDTO;
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
