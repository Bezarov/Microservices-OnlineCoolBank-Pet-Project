package com.example.userscomponent.feign;

import com.example.userscomponent.config.UsersAppComponentConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AppRegistryComponentClientFallback implements AppRegistryComponentClient {
    @Override
    public void registerComponent(UsersAppComponentConfig usersAppComponentConfig) {
    }

    @Override
    public ResponseEntity<String> deregisterComponent(UUID componentId) {
        return null;
    }
}
