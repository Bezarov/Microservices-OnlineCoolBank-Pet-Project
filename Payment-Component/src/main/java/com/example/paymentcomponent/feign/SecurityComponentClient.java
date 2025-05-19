package com.example.paymentcomponent.feign;

import com.example.paymentcomponent.dto.AuthRequestDTO;
import com.example.paymentcomponent.dto.TokenAuthRequestDTO;
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
