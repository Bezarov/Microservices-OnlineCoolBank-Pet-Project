package com.example.userscomponent.feign;

import com.example.userscomponent.dto.AuthRequestDTO;
import com.example.userscomponent.dto.TokenAuthRequestDTO;
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
