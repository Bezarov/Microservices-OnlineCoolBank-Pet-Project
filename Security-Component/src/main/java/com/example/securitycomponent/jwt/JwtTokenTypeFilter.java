package com.example.securitycomponent.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

@Component
public class JwtTokenTypeFilter extends OncePerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(JwtTokenTypeFilter.class);
    private final JwtUtil jwtUtil;

    public JwtTokenTypeFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestTokenHeader = request.getHeader("Authorization");
        String jwtToken = null;
        String tokenType = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            logger.info("Extracting token type");
            tokenType = jwtUtil.extractClaim(jwtToken, claims -> claims.get("tokenType", String.class));

            if (request.getRequestURI().startsWith("/component") && !"component".equals(tokenType)) {
                logger.error("Extracted token type is: {}, Access to this resource denied", tokenType);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authorization failed");
                return;
            }

            if (!request.getRequestURI().startsWith("/component") && !"user".equals(tokenType)) {
                logger.error("Extracted token type is: {}, Access to this resource denied", tokenType);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authorization failed");
                return;
            }
        }
        logger.info("Extracted token type Authorized successfully");
        filterChain.doFilter(request, response);
    }
}
