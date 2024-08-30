package com.example.apigatewaycomponent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.apigatewaycomponent.feign")
public class ApiGatewayComponentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayComponentApplication.class, args);
    }

}
