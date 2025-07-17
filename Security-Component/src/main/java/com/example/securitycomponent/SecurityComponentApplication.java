package com.example.securitycomponent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.securitycomponent.feign")
@EnableScheduling
public class SecurityComponentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityComponentApplication.class, args);
    }

}
