package com.example.cardcomponent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.cardcomponent.feign")
public class CardComponentApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardComponentApplication.class, args);
    }

}
