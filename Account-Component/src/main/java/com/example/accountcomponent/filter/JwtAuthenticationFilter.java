package com.example.accountcomponent.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER = "Bearer ";
    private static final String HEADER = "Authorization";

    private final JwtVerifier jwtVerifier;

    public JwtAuthenticationFilter(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestJwtToken = extractAuthorizationHeader(request);
        String requestURI = request.getRequestURI();

        if (requestJwtToken != null && !"null".equals(requestJwtToken) && requestURI.startsWith("/account/")) {
            LOGGER.debug("Intercepted a request with token: {} to URI: {}", requestJwtToken, requestURI);
            try {
                String subject = jwtVerifier.verify(requestJwtToken);

                setAuthentication(subject, request);
                LOGGER.debug("Security context holder authentication was successfully set");
                filterChain.doFilter(request, response);
                return;
            } catch (AuthenticationException authException) {
                SecurityContextHolder.clearContext();
                response.setHeader("Content-Type", "application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("""
                        "security-error": "%s"
                        """.formatted(authException.getMessage()));
                response.getWriter().close();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractAuthorizationHeader(final HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HEADER);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER))
            return authorizationHeader.substring(BEARER.length());
        return authorizationHeader;
    }

    private void setAuthentication(final String subject, final HttpServletRequest request) {
        try {
            UsernamePasswordAuthenticationToken usernamePassAuthToken = new UsernamePasswordAuthenticationToken(
                    subject, null, new ArrayList<>());
            usernamePassAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(usernamePassAuthToken);
        } catch (Exception exception) {
            LOGGER.error("Error while setting authentication context", exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while setting authentication context");
        }
    }
}