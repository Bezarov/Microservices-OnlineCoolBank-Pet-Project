package com.example.accountcomponent.feign;

import com.example.accountcomponent.config.AccountAppComponentConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AppRegistryComponentClientFallback implements AppRegistryComponentClient {
    @Override
    public void registerComponent(AccountAppComponentConfig accountAppComponentConfig) {
    }

    @Override
    public ResponseEntity<String> deregisterComponent(UUID componentId) {
        return null;
    }
}
