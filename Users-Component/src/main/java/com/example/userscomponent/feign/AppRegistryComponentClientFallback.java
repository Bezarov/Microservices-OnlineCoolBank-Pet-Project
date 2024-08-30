package com.example.userscomponent.feign;

import com.example.userscomponent.dto.UsersAppComponentConfigDTO;
import org.springframework.stereotype.Component;

@Component
public class AppRegistryComponentClientFallback implements AppRegistryComponentClient{
    @Override
    public void registerComponent(UsersAppComponentConfigDTO usersAppComponentConfigDTO) {

    }
}
