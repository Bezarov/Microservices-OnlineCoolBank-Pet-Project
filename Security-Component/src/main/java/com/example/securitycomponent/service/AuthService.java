package com.example.securitycomponent.service;


import com.example.securitycomponent.dto.AuthRequestDTO;

public interface AuthService {
    String authenticateUser(AuthRequestDTO authRequestDTO);

    String authenticateComponent(AuthRequestDTO authRequestDTO);
}
