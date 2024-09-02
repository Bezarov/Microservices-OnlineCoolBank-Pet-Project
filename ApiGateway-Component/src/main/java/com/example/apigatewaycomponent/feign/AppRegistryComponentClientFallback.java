package com.example.apigatewaycomponent.feign;

import com.example.apigatewaycomponent.dto.ApiGatewayAppComponentConfigDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AppRegistryComponentClientFallback implements AppRegistryComponentClient {
    @Override
    public void registerComponent(ApiGatewayAppComponentConfigDTO apiGatewayAppComponentConfigDTO) {
    }

    @Override
    public ResponseEntity<String> deregisterComponent(UUID componentId) {
        return null;
    }
}
