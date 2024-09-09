package com.example.appregistrycomponent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableConfigServer
@EnableFeignClients(basePackages = "com.example.appregistrycomponent.feign")
public class AppRegistryComponentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppRegistryComponentApplication.class, args);
    }

}
