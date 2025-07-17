package com.example.appregistrycomponent.config;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AppRegistryComponentConfig {
    private UUID componentId;
    private String componentSecret;
    private volatile String jwtToken;

    public UUID getComponentId() {
        return componentId;
    }

    public void setComponentId(UUID componentId) {
        this.componentId = componentId;
    }

    public String getComponentSecret() {
        return componentSecret;
    }

    public void setComponentSecret(String componentSecret) {
        this.componentSecret = componentSecret;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
}
