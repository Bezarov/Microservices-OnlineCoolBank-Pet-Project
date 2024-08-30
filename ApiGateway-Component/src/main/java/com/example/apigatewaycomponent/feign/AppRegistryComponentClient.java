package com.example.apigatewaycomponent.feign;

import com.example.apigatewaycomponent.dto.ApiGatewayAppComponentConfigDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Qualifier("AppRegistry-Components")
@FeignClient(name = "APPREGISTRY-COMPONENTS", url = "http://localhost:8601/components", fallback = AppRegistryComponentClientFallback.class)
public interface AppRegistryComponentClient {
    @PostMapping
    void registerComponent(@RequestBody ApiGatewayAppComponentConfigDTO apiGatewayAppComponentConfigDTO);
}
