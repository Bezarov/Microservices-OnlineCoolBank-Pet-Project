package com.example.securitycomponent.service;

import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.dto.AuthResponseDTO;
import com.example.securitycomponent.dto.TokenAuthRequestDTO;
import com.example.securitycomponent.exception.CustomKafkaException;
import com.example.securitycomponent.jwt.JwtTokenAuthenticator;
import com.example.securitycomponent.jwt.JwtTokenTypeAuthorizer;
import com.example.securitycomponent.jwt.JwtUtil;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class KafkaAuthServiceImpl implements KafkaAuthService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaAuthServiceImpl.class);
    private final AuthDetailsService authDetailsService;
    private final JwtTokenAuthenticator jwtTokenAuthenticator;
    private final JwtTokenTypeAuthorizer jwtTokenTypeAuthorizer;
    private final JwtUtil jwtUtil;
    private final KafkaTemplate<String, AuthResponseDTO> responseDTOKafkaTemplate;
    private final KafkaTemplate<String, SecurityContext> responseSecurityContextKafkaTemplate;

    public KafkaAuthServiceImpl(AuthDetailsService authDetailsService, JwtTokenAuthenticator jwtTokenAuthenticator,
                                JwtTokenTypeAuthorizer jwtTokenTypeAuthorizer, JwtUtil jwtUtil,
                                KafkaTemplate<String, AuthResponseDTO> responseDTOKafkaTemplate,
                                KafkaTemplate<String, SecurityContext> responseSecurityContextKafkaTemplate) {
        this.authDetailsService = authDetailsService;
        this.jwtTokenAuthenticator = jwtTokenAuthenticator;
        this.jwtTokenTypeAuthorizer = jwtTokenTypeAuthorizer;
        this.jwtUtil = jwtUtil;
        this.responseDTOKafkaTemplate = responseDTOKafkaTemplate;
        this.responseSecurityContextKafkaTemplate = responseSecurityContextKafkaTemplate;
    }

    @Override
    @KafkaListener(topics = "user-authentication", groupId = "security-component",
            containerFactory = "usersAuthRequestDTOKafkaListenerFactory")
    public void authenticateUser(AuthRequestDTO authRequestDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: user-authentication with correlation id: {} ", correlationId);
        try {
            logger.info("Authenticating user with email: {}", authRequestDTO.principal());
            authDetailsService.authenticateUser(authRequestDTO);
            logger.info("Authentication successfully for user with email: {}", authRequestDTO.principal());
            logger.info("Trying to generate user token for credentials: {}", authRequestDTO);
            String jwtToken = jwtUtil.userTokenGenerator(authRequestDTO.principal().toString());
            logger.info("Generated JWT Token: {}", jwtToken);
            logger.info("Trying to create topic: user-authentication-response with correlation id: {} ", correlationId);
            ProducerRecord<String, AuthResponseDTO> responseTopic = new ProducerRecord<>(
                    "user-authentication-response", null, new AuthResponseDTO(jwtToken));
            responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
            responseDTOKafkaTemplate.send(responseTopic);
            logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
        } catch (AuthenticationException error) {
            logger.error("Authentication failed for User with Email: {} Password: {}",
                    authRequestDTO.principal(), authRequestDTO.credentials());
            throw new CustomKafkaException(HttpStatus.UNAUTHORIZED,
                    "Authentication failed \nInvalid Email or Password correlationId:" + correlationId);
        }
    }

    @Override
    @KafkaListener(topics = "user-token-authentication", groupId = "security-component",
            containerFactory = "mapStringToStringKafkaListenerFactory")
    public void authenticateUserToken(Map<String, String> mapJwtTokenToRequestURI,
                                      @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: user-token-authentication with correlation id: {} ", correlationId);
        String jwtToken = mapJwtTokenToRequestURI.keySet().iterator().next();
        String requestURI = mapJwtTokenToRequestURI.get(jwtToken);

        try {
            logger.info("Authenticating user Token: {}", jwtToken);
            SecurityContext responseSecurityContext = jwtTokenAuthenticator.doTokenAuthentication(jwtToken);
            logger.info("Authentication successfully");
            logger.info("Authorizing user Token: {} and requested URI: {}", jwtToken, requestURI);
            jwtTokenTypeAuthorizer.doTokenAuthorization(new TokenAuthRequestDTO(jwtToken, requestURI));
            logger.info("Authentication and authorization successfully");
            logger.info("Trying to create topic: user-token-authentication-response with correlation id: {} ", correlationId);
            ProducerRecord<String, SecurityContext> responseTopic = new ProducerRecord<>(
                    "user-token-authentication-response", null, responseSecurityContext);
            responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
            responseSecurityContextKafkaTemplate.send(responseTopic);
            logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
        } catch (ResponseStatusException exception){
            throw new CustomKafkaException(exception.getStatusCode(), exception.getReason() + " correlationId:" + correlationId);
        }

    }
}