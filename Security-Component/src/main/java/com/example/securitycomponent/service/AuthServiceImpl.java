package com.example.securitycomponent.service;

import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.dto.AuthResponseDTO;
import com.example.securitycomponent.dto.JwksSpecificInfoDTO;
import com.example.securitycomponent.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final AuthDetailsService authDetailsService;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(AuthDetailsService authDetailsService, JwtUtil jwtUtil) {
        this.authDetailsService = authDetailsService;
        this.jwtUtil = jwtUtil;
    }


    @Override
    public AuthResponseDTO authenticateComponent(AuthRequestDTO authRequestDTO) {
        LOGGER.info("Authenticating component with ID: \"{}\"", authRequestDTO.principal());
        authDetailsService.authenticateComponent(authRequestDTO);
        LOGGER.info("Authentication successfully for Component with ID: \"{}\"", authRequestDTO.principal());

        LOGGER.info("Trying to generate component token for credentials: \"{}\"", authRequestDTO);
        String jwtToken = jwtUtil.generateComponentToken(authRequestDTO.principal().toString());
        LOGGER.info("Generated JWT Token: \"{}\"", jwtToken);
        return new AuthResponseDTO(jwtToken);
    }

    @Override
    public JwksSpecificInfoDTO getJwks(AuthRequestDTO authRequestDTO) {
        LOGGER.info("Authenticating component with ID: \"{}\"", authRequestDTO.principal());
        authDetailsService.authenticateComponent(authRequestDTO);
        LOGGER.info("Authentication successfully for Component with ID: \"{}\"", authRequestDTO.principal());
        JwksSpecificInfoDTO jwksSpecificInfoDTO = jwtUtil.getJwksSpecificInfo();
        LOGGER.info("Generated JWKS spec info: {}", jwksSpecificInfoDTO);
        return jwksSpecificInfoDTO;
    }
}
