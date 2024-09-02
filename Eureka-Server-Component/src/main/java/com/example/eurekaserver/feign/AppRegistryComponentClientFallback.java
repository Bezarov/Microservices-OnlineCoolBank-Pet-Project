package com.example.eurekaserver.feign;

import com.example.eurekaserver.dto.EurekaServerAppComponentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AppRegistryComponentClientFallback implements AppRegistryComponentClient{
    @Override
    public void registerComponent(EurekaServerAppComponentDTO eurekaServerAppComponentDTO) {

    }

    @Override
    public ResponseEntity<String> deregisterComponent(UUID componentId) {
        return null;
    }
}
