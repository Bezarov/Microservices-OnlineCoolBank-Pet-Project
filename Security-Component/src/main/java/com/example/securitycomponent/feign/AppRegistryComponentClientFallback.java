package com.example.securitycomponent.feign;

import com.example.securitycomponent.dto.AppComponentDTO;
import com.example.securitycomponent.dto.SecurityAppComponentConfigDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Component
public class AppRegistryComponentClientFallback implements AppRegistryComponentClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppRegistryComponentClientFallback.class);


    @Override
    public void registerComponent(SecurityAppComponentConfigDTO securityAppComponentConfigDTO) {
    }

    @Override
    public Optional<AppComponentDTO> findById(UUID componentId) {
        appRegistryComponentFallback(UUID.randomUUID());
        return Optional.empty();
    }

    @Override
    public ResponseEntity<String> deregisterComponent(UUID componentId) {
        return null;
    }

    @Override
    public void appRegistryComponentFallback(UUID componentId) {
        LOGGER.error("AppRegistry Component is unreachable authentication failed for component with id: {}" +
                " generate exception", componentId);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Service is unreachable please try again later.");
    }
}
