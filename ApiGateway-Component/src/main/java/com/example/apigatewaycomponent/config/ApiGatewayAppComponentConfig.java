package com.example.apigatewaycomponent.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ApiGatewayAppComponentConfig {
    @JsonProperty("componentName")
    private String componentName;
    @JsonProperty("componentId")
    private UUID componentId;
    @JsonProperty("componentSecret")
    private String componentSecret;
    private volatile String jwtToken;

    public ApiGatewayAppComponentConfig() {
    }

    public ApiGatewayAppComponentConfig(String componentName, UUID componentId, String componentSecret) {
        this.componentName = componentName;
        this.componentId = componentId;
        this.componentSecret = componentSecret;
    }

    public UUID getComponentId() {
        return componentId;
    }

    public void setComponentId(UUID componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
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

    @Override
    public String toString() {
        return "ApiGatewayAppComponentConfig{" +
                "componentName='" + componentName + '\'' +
                ", componentId=" + componentId +
                ", componentSecret='" + componentSecret + '\'' +
                ", jwtToken='" + jwtToken + '\'' +
                '}';
    }
}
