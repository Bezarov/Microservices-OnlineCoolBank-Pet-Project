package com.example.cardcomponent.feign;

import com.example.cardcomponent.dto.CardAppComponentConfigDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AppRegistryComponentClientFallback implements AppRegistryComponentClient{
    @Override
    public void registerComponent(CardAppComponentConfigDTO cardAppComponentConfigDTO) {

    }

    @Override
    public ResponseEntity<String> deregisterComponent(UUID componentId) {
        return null;
    }
}
