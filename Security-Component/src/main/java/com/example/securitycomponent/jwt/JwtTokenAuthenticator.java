package com.example.securitycomponent.jwt;

import com.example.securitycomponent.dto.AppComponentDTO;
import com.example.securitycomponent.dto.UsersDTO;
import com.example.securitycomponent.service.AuthDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

@Component
public class JwtTokenAuthenticator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenAuthenticator.class);
    private final AuthDetailsService authDetailsService;
    private final JwtUtil jwtUtil;

    public JwtTokenAuthenticator(AuthDetailsService authDetailsService, JwtUtil jwtUtil) {
        this.authDetailsService = authDetailsService;
        this.jwtUtil = jwtUtil;
    }

    public SecurityContext doTokenAuthentication(String jwtToken) {
        try {
            LOGGER.info("Extracting identity from Token: \"{}\"", jwtToken);
            String principal = jwtUtil.getIdentityFromToken(jwtToken);
            LOGGER.debug("JWT Token issued to: \"{}\"", principal);
            if (principal != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.isUserToken(jwtToken) && jwtUtil.validateUserToken(jwtToken)) {
                    LOGGER.debug("Authenticating extracted user email: \"{}\" from Token", principal);
                    UsersDTO usersDTO = authDetailsService.authenticateUserToken(principal);
                    LOGGER.debug("Setting security context holder");
                    return setUserAuthentication(usersDTO);
                } else if (jwtUtil.isComponentToken(jwtToken) && jwtUtil.validateComponentToken(jwtToken)) {
                    LOGGER.debug("Authenticating extracted component id: \"{}\" from Token", principal);
                    AppComponentDTO appComponentDTO = authDetailsService.authenticateComponentToken(principal);
                    LOGGER.debug("Setting security context holder");
                    return setComponentAuthentication(appComponentDTO);
                }
            }
        } catch (ExpiredJwtException exception) {
            LOGGER.warn("Expired JWT token: \"{}\"", jwtToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT token is expired, refresh it");
        } catch (JwtException exception) {
            LOGGER.warn("Unable to extract from JWT Token identity: \"{}\"", jwtToken);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to process JWT Token, please get an acceptable JWT Token");
        }
        return SecurityContextHolder.getContext();
    }

    private SecurityContext setUserAuthentication(UsersDTO usersDTO) {
        try {
            UsernamePasswordAuthenticationToken userNamePassAuthToken = new UsernamePasswordAuthenticationToken(
                    usersDTO.getEmail(), null, new ArrayList<>());

            SecurityContextHolder.getContext().setAuthentication(userNamePassAuthToken);
            LOGGER.debug("Security context holder successfully set");
            return SecurityContextHolder.getContext();
        } catch (Exception exception) {
            LOGGER.error("Error while setting user authentication context", exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while setting authentication context");
        }
    }

    private SecurityContext setComponentAuthentication(AppComponentDTO appComponentDTO) {
        try {
            UsernamePasswordAuthenticationToken userNamePassAuthToken = new UsernamePasswordAuthenticationToken(
                    appComponentDTO.getComponentId(), appComponentDTO.getComponentSecret(), new ArrayList<>());

            SecurityContextHolder.getContext().setAuthentication(userNamePassAuthToken);
            LOGGER.debug("Security context holder successfully set");
            return SecurityContextHolder.getContext();
        } catch (Exception exception) {
            LOGGER.error("Error while setting component authentication context", exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while setting authentication context");
        }
    }
}