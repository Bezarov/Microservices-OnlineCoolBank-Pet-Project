package com.example.accountcomponent.feign;

import com.example.accountcomponent.dto.AuthRequestDTO;
import com.example.accountcomponent.dto.TokenAuthRequestDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Qualifier("Security-Components")
@FeignClient(name = "SECURITY-COMPONENTS", fallback = SecurityComponentClientFallback.class)
public interface SecurityComponentClient {
    @PostMapping("auth/component")
    String authenticateComponent(@RequestBody AuthRequestDTO authRequestDTO);

    @PostMapping("auth/component/token")
    Boolean authenticateComponentToken(@RequestBody TokenAuthRequestDTO tokenAuthRequestDTO);
}
