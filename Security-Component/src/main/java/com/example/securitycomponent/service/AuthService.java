package com.example.securitycomponent.service;


import com.example.securitycomponent.dto.AuthRequestDTO;

public interface AuthService {

    String authenticateComponent(AuthRequestDTO authRequestDTO);

    Boolean authenticateComponentToken(String jwtToken, String requestURI);
}
