package com.example.eurekaserver.filter;

import com.example.eurekaserver.dto.TokenAuthRequestDTO;
import com.example.eurekaserver.feign.SecurityComponentClient;
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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final SecurityComponentClient securityComponentClient;
    private static final String BEARER = "Bearer ";
    private static final String HEADER = "Authorization";

    public JwtAuthenticationFilter(@Qualifier("Security-Components") SecurityComponentClient securityComponentClient) {
        this.securityComponentClient = securityComponentClient;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestJwtToken = authorizationHeaderExtractor(request);
        String requestURI = request.getRequestURI();

        if (requestJwtToken != null && requestURI.startsWith("/eureka/")) {
            TokenAuthRequestDTO authRequestDTO = new TokenAuthRequestDTO(requestJwtToken, requestURI);
            try {
                LOGGER.info("Trying to authenticate component token: {} in: Security-Components", requestJwtToken);
                if (Boolean.TRUE.equals(securityComponentClient.authenticateComponentToken(authRequestDTO))) {
                    LOGGER.info("JWT Token authenticated successfully");
                    setAuthentication(request);
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (FeignException feignResponseError) {
                LOGGER.error("Got feign exception during authentication: {}", feignResponseError.contentUTF8());
                response.setStatus(feignResponseError.status());
                response.getWriter().write(feignResponseError.contentUTF8());
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

    private void setAuthentication(final HttpServletRequest request) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthToken = new UsernamePasswordAuthenticationToken(
                null, null, null);
        usernamePasswordAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthToken);
    }
}