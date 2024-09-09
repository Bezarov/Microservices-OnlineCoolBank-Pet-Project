package com.example.securitycomponent.service;


import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.dto.TokenAuthRequestDTO;

public interface AuthService {

    String authenticateComponent(AuthRequestDTO authRequestDTO);

    Boolean authenticateComponentToken(TokenAuthRequestDTO tokenAuthRequestDTO);
}
