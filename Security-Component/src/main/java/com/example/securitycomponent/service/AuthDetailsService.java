package com.example.securitycomponent.service;

import com.example.securitycomponent.dto.AuthRequestDTO;

public interface AuthDetailsService {

    void authenticateUser(AuthRequestDTO authRequestDTO);

    void authenticateComponent(AuthRequestDTO authRequestDTO);
}
