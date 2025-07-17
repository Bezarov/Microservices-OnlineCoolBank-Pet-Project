package com.example.paymentcomponent.feign;

import com.example.paymentcomponent.config.PaymentAppComponentConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AppRegistryComponentClientFallback implements AppRegistryComponentClient {
    @Override
    public void registerComponent(PaymentAppComponentConfig paymentAppComponentConfig) {

    }

    @Override
    public ResponseEntity<String> deregisterComponent(UUID componentId) {
        return null;
    }
}
