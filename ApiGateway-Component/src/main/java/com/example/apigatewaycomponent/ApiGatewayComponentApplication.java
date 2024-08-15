package com.example.apigatewaycomponent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayComponentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayComponentApplication.class, args);
    }

}
