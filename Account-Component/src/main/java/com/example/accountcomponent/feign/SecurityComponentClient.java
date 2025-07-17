package com.example.accountcomponent.feign;

import com.example.accountcomponent.dto.AuthResponseDTO;
import com.example.accountcomponent.dto.AuthRequestDTO;
import com.example.accountcomponent.dto.JwksSpecificInfoDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Qualifier("Security-Components")
@FeignClient(name = "SECURITY-COMPONENTS", fallback = SecurityComponentClientFallback.class)
public interface SecurityComponentClient {
    @PostMapping("auth/component")
    AuthResponseDTO authenticateComponent(@RequestBody AuthRequestDTO authRequestDTO);

    @PostMapping("auth/jwks")
    JwksSpecificInfoDTO getActualJwks(@RequestBody AuthRequestDTO authRequestDTO);
}
