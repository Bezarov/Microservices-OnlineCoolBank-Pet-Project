package com.example.apigatewaycomponent.feign;

import com.example.apigatewaycomponent.dto.AuthRequestDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Qualifier("Security-Components")
@FeignClient(name = "SECURITY-COMPONENTS", url = "http://localhost:8501/auth", fallback = SecurityComponentClientFallback.class)
public interface SecurityComponentClient {
    @Retryable(maxAttempts = 2, backoff = @Backoff(delay = 1000))
    @PostMapping("/component")
    String authenticateComponent(@RequestBody AuthRequestDTO authRequestDTO);
}
