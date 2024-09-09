package com.example.appregistrycomponent.feign;

import com.example.appregistrycomponent.dto.TokenAuthRequestDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Qualifier("Security-Components")
@FeignClient(name = "SECURITY-COMPONENTS", url = "http://localhost:8501/auth", fallback = SecurityComponentClientFallback.class)
public interface SecurityComponentClient {

    @PostMapping("/component/token")
    Boolean authenticateComponentToken(@RequestBody TokenAuthRequestDTO tokenAuthRequestDTO);
}
