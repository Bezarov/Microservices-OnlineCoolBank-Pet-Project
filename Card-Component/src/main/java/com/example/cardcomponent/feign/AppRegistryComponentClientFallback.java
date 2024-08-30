package com.example.cardcomponent.feign;

import com.example.cardcomponent.dto.CardAppComponentConfigDTO;
import org.springframework.stereotype.Component;

@Component
public class AppRegistryComponentClientFallback implements AppRegistryComponentClient{
    @Override
    public void registerComponent(CardAppComponentConfigDTO cardAppComponentConfigDTO) {

    }
}
