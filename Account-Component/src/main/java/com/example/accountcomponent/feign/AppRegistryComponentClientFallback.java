package com.example.accountcomponent.feign;

import com.example.accountcomponent.dto.AccountAppComponentConfigDTO;
import org.springframework.stereotype.Component;

@Component
public class AppRegistryComponentClientFallback implements AppRegistryComponentClient {
    @Override
    public void registerComponent(AccountAppComponentConfigDTO accountAppComponentConfigDTO) {

    }
}
