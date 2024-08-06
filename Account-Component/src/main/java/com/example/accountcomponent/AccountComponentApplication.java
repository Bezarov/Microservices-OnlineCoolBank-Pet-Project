package com.example.accountcomponent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.FeignClient;

@SpringBootApplication
@FeignClient
public class AccountComponentApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountComponentApplication.class, args);
    }
}
