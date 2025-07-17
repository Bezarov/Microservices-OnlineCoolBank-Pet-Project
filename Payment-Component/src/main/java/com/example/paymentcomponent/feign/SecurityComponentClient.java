package com.example.paymentcomponent.feign;

import com.example.paymentcomponent.dto.AuthRequestDTO;
import com.example.paymentcomponent.dto.AuthResponseDTO;
import com.example.paymentcomponent.dto.JwksSpecificInfoDTO;
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
