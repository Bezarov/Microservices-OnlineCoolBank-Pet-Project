package com.example.apigatewaycomponent.feign;

import com.example.apigatewaycomponent.dto.ApiGatewayAppComponentConfigDTO;
import org.springframework.stereotype.Component;

@Component
public class AppRegistryComponentClientFallback implements AppRegistryComponentClient {
    @Override
    public void registerComponent(ApiGatewayAppComponentConfigDTO apiGatewayAppComponentConfigDTO) {
    }
}
