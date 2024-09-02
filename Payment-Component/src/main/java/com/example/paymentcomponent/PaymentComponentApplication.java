package com.example.paymentcomponent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.paymentcomponent.feign")
public class PaymentComponentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentComponentApplication.class, args);
    }
}