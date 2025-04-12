package com.example.apigatewaycomponent.filter;

import com.example.apigatewaycomponent.service.SecurityGatewayService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final SecurityGatewayService securityGatewayService;
    private static final long REQUEST_TIMEOUT = 6;
    private static final String BEARER = "Bearer ";
    private static final String HEADER = "Authorization";

    public JwtAuthenticationFilter(SecurityGatewayService securityGatewayService) {
        this.securityGatewayService = securityGatewayService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestJwtToken = authorizationHeaderExtractor(request);
        String requestURI = request.getRequestURI();

        if (requestJwtToken != null && requestURI.startsWith("/api/")) {
            LOGGER.debug("JwtAuthenticationFilter intercepted request to URI: {}", requestURI);
            CompletableFuture<ResponseEntity<Object>> tokenAuthResponse =
                    securityGatewayService.authenticateUserToken(requestJwtToken, requestURI);
            try {
                ResponseEntity<Object> authResponse = tokenAuthResponse.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
                if (authResponse != null && authResponse.getBody() instanceof ResponseEntity<?> nestedResponse &&
                        nestedResponse.getBody() instanceof SecurityContextImpl securityContext) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            securityContext.getAuthentication().getPrincipal(), securityContext.getAuthentication().getCredentials(),
                            securityContext.getAuthentication().getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    LOGGER.debug("User JWT Token authenticated successfully: {} for URI: {}", requestJwtToken, requestURI);
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (InterruptedException | ExecutionException | TimeoutException exception) {
                String exceptionMessage = exception.getMessage().replaceAll(".*\"(.*?)\".*", "$1");
                LOGGER.warn("Security component error: {}", exceptionMessage);
                response.setHeader("Content-Type", "application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(String.format("""
                        {
                        "security-error": "%s"
                        }
                        """, exceptionMessage));
                response.getWriter().close();
                Thread.currentThread().interrupt();
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String authorizationHeaderExtractor(final HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HEADER);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            return authorizationHeader.substring(BEARER.length());
        }
        return authorizationHeader;
    }
}