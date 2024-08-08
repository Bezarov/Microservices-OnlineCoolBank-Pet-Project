package com.example.appregistrycomponent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;


@SpringBootApplication
@EnableConfigServer
public class AppRegistryComponentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppRegistryComponentApplication.class, args);
    }

}
