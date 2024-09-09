package com.example.appregistrycomponent.feign;

import com.example.appregistrycomponent.dto.TokenAuthRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class SecurityComponentClientFallback implements SecurityComponentClient {
    @Override
    public Boolean authenticateComponentToken(TokenAuthRequestDTO tokenAuthRequestDTO) {
        return null;
    }
}
