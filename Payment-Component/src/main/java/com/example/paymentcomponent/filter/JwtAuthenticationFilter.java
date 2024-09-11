package com.example.paymentcomponent.filter;

import com.example.paymentcomponent.dto.TokenAuthRequestDTO;
import com.example.paymentcomponent.feign.SecurityComponentClient;
import feign.FeignException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final SecurityComponentClient securityComponentClient;

    public JwtAuthenticationFilter(@Qualifier("Security-Components") SecurityComponentClient securityComponentClient) {
        this.securityComponentClient = securityComponentClient;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestJwtToken = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();
        if (requestJwtToken != null && requestJwtToken.startsWith("Bearer ") && requestURI.startsWith("/users/")) {
            try {
                logger.info("Trying to authenticate component token: {} in: Security-Components", request.getHeader("Authorization"));
                Boolean isAuthenticated = securityComponentClient.authenticateComponentToken(
                        new TokenAuthRequestDTO(requestJwtToken.substring(7), requestURI));
                if (isAuthenticated) {
                    logger.info("JWT Token authenticated successfully");
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(null, null, null);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    filterChain.doFilter(request, response);
                }
            } catch (FeignException feignResponseError) {
                logger.error("Got feign exception during authentication: " + feignResponseError.contentUTF8());
                response.setStatus(feignResponseError.status());
                response.getWriter().write(feignResponseError.contentUTF8());
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
