package com.example.accountcomponent.feign;

import com.example.accountcomponent.dto.AccountAppComponentConfigDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Qualifier("AppRegistry-Components")
@FeignClient(name = "APPREGISTRY-COMPONENTS", url = "http://localhost:8601/components", fallback = AppRegistryComponentClientFallback.class)
public interface AppRegistryComponentClient {
    @PostMapping
    void registerComponent(@RequestBody AccountAppComponentConfigDTO accountAppComponentConfigDTO);
}
