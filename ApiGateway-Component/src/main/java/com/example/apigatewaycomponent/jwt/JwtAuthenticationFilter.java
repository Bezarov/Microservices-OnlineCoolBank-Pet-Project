package com.example.apigatewaycomponent.jwt;

import com.example.apigatewaycomponent.service.SecurityGatewayService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final SecurityGatewayService securityGatewayService;

    public JwtAuthenticationFilter(SecurityGatewayService securityGatewayService) {
        this.securityGatewayService = securityGatewayService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestJwtToken = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();

        if (requestJwtToken != null && requestJwtToken.startsWith("Bearer ") && requestURI.startsWith("/api/")) {
            CompletableFuture<ResponseEntity<Object>> tokenAuthResponse = securityGatewayService.authenticateUserToken(
                    requestJwtToken.substring(7), requestURI);

            tokenAuthResponse.thenAccept(authResponse -> {
                if (authResponse != null && authResponse.getBody() instanceof SecurityContext) {
                    SecurityContextHolder.setContext((SecurityContext) authResponse.getBody());
                    logger.info("User authenticated successfully for URI: {}", requestURI);
                }
            }).join();
        }
        filterChain.doFilter(request, response);
    }
}