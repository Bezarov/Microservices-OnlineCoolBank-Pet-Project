package com.example.securitycomponent.jwt;

import com.example.securitycomponent.model.AppComponent;
import com.example.securitycomponent.model.Users;
import com.example.securitycomponent.service.AuthDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);
    private final AuthDetailsService authDetailsService;
    private final JwtUtil jwtUtil;

    public JwtRequestFilter(AuthDetailsService authDetailsService, JwtUtil jwtUtil) {
        this.authDetailsService = authDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");
        logger.debug("Received Authorization Header: " + requestTokenHeader);

        String principal = null;
        String receivedJwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            receivedJwtToken = requestTokenHeader.substring(7);
            try {
                principal = jwtUtil.getIdentityFromToken(receivedJwtToken);
                logger.debug("JWT Token issued to: " + principal);
            } catch (IllegalArgumentException e) {
                logger.warn("Unable to extract JWT token from request");
                throw new ResponseStatusException(HttpStatus.NON_AUTHORITATIVE_INFORMATION,
                        "Unable to extract JWT token from request");
            } catch (ExpiredJwtException e) {
                logger.warn("Received JWT token is Expired");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Expired JWT token");
            }
        } else {
            logger.warn("Received request authentication header doesn't have Bearer");
        }

        if (principal != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.isUserToken(receivedJwtToken) && jwtUtil.validateUserToken(receivedJwtToken)) {
                logger.debug("JWT Token is not expired");
                logger.debug("Authenticating user token");
                Users user = authDetailsService.authenticateUserToken(principal);
                logger.debug("Setting security context holder");
                setUserAuthentication(request, user);
            } else if (jwtUtil.isComponentToken(receivedJwtToken) && jwtUtil.validateComponentToken(receivedJwtToken)) {
                logger.debug("JWT Token is not expired");
                logger.debug("Authenticating component token");
                AppComponent appComponent = authDetailsService.authenticateComponentToken(principal);
                logger.debug("Setting security context holder");
                setComponentAuthentication(request, appComponent);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void setUserAuthentication(HttpServletRequest request, Users user) {
        try {
            UsernamePasswordAuthenticationToken userNamePassAuthToken = new UsernamePasswordAuthenticationToken(
                    user.getEmail(), user.getPassword(), new ArrayList<>());
            userNamePassAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(userNamePassAuthToken);
            logger.debug("Security context holder successfully set");
        } catch (Exception exception) {
            logger.error("Error while setting user authentication context");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while setting authentication context");
        }
    }

    private void setComponentAuthentication(HttpServletRequest request, AppComponent appComponent) {
        try {
            UsernamePasswordAuthenticationToken userNamePassAuthToken = new UsernamePasswordAuthenticationToken(
                    appComponent.getComponentId(), appComponent.getComponentSecret(), new ArrayList<>());
            userNamePassAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(userNamePassAuthToken);
            logger.debug("Security context holder successfully set");
        } catch (Exception exception) {
            logger.error("Error while setting component authentication context");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while setting authentication context");
        }
    }
}

