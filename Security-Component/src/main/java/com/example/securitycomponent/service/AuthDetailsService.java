package com.example.securitycomponent.service;

import com.example.securitycomponent.dto.AppComponentDTO;
import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.dto.UsersDTO;

public interface AuthDetailsService {

    void authenticateUser(AuthRequestDTO authRequestDTO);

    void authenticateComponent(AuthRequestDTO authRequestDTO);

    UsersDTO authenticateUserToken(String principal);

    AppComponentDTO authenticateComponentToken(String principal);
}
