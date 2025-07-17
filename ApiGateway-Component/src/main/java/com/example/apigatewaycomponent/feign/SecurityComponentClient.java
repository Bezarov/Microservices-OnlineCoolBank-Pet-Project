package com.example.apigatewaycomponent.feign;

import com.example.apigatewaycomponent.dto.AuthRequestDTO;
import com.example.apigatewaycomponent.dto.AuthResponseDTO;
import com.example.apigatewaycomponent.dto.JwksSpecificInfoDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Qualifier("Security-Components")
@FeignClient(name = "SECURITY-COMPONENTS", fallback = SecurityComponentClientFallback.class)
public interface SecurityComponentClient {
    @Retryable(maxAttempts = 2, backoff = @Backoff(delay = 1000))
    @PostMapping("auth/component")
    AuthResponseDTO authenticateComponent(@RequestBody AuthRequestDTO authRequestDTO);

    @PostMapping("auth/jwks")
    JwksSpecificInfoDTO getActualJwks(@RequestBody AuthRequestDTO authRequestDTO);
}
