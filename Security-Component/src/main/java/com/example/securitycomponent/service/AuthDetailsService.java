package com.example.securitycomponent.service;

import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.model.AppComponent;
import com.example.securitycomponent.model.Users;

public interface AuthDetailsService {

    void authenticateUser(AuthRequestDTO authRequestDTO);

    void authenticateComponent(AuthRequestDTO authRequestDTO);

    Users authenticateUserToken(String principal);

    AppComponent authenticateComponentToken(String principal);
}
