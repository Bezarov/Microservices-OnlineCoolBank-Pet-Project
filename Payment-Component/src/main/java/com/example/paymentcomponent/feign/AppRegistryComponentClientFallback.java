package com.example.paymentcomponent.feign;

import com.example.paymentcomponent.dto.PaymentAppComponentConfigDTO;
import org.springframework.stereotype.Component;

@Component
public class AppRegistryComponentClientFallback implements AppRegistryComponentClient{
    @Override
    public void registerComponent(PaymentAppComponentConfigDTO paymentAppComponentConfigDTO) {

    }
}
