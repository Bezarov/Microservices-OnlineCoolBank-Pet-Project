package com.example.paymentcomponent.feign;

import com.example.paymentcomponent.dto.PaymentAppComponentConfigDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AppRegistryComponentClientFallback implements AppRegistryComponentClient{
    @Override
    public void registerComponent(PaymentAppComponentConfigDTO paymentAppComponentConfigDTO) {

    }

    @Override
    public ResponseEntity<String> deregisterComponent(UUID componentId) {
        return null;
    }
}
