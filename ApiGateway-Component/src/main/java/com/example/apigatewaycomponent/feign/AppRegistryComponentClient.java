package com.example.apigatewaycomponent.feign;

import com.example.apigatewaycomponent.config.ApiGatewayAppComponentConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Qualifier("AppRegistry-Components")
@FeignClient(name = "APPREGISTRY-COMPONENTS", fallback = AppRegistryComponentClientFallback.class)
public interface AppRegistryComponentClient {
    @PostMapping("component")
    void registerComponent(@RequestBody ApiGatewayAppComponentConfig apiGatewayAppComponentConfig);

    @DeleteMapping("component/by-id/{componentId}")
    ResponseEntity<String> deregisterComponent(@PathVariable UUID componentId);
}
