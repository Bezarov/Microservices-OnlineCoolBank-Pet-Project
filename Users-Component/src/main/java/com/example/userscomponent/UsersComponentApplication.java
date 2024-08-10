package com.example.userscomponent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UsersComponentApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsersComponentApplication.class, args);
    }
}
