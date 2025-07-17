package com.example.securitycomponent.service;


import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.dto.AuthResponseDTO;
import com.example.securitycomponent.dto.JwksSpecificInfoDTO;

public interface AuthService {

    AuthResponseDTO authenticateComponent(AuthRequestDTO authRequestDTO);

    JwksSpecificInfoDTO getJwks(AuthRequestDTO authRequestDTO);
}
