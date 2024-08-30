package com.example.accountcomponent.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class AccountAppComponentConfigDTO {
    @JsonProperty("componentName")
    private String componentName;
    @JsonProperty("componentId")
    private UUID componentId;
    @JsonProperty("componentSecret")
    private String componentSecret;

    @JsonIgnore
    private String token;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "AccountAppComponentConfigDTO{" +
                "componentName='" + componentName + '\'' +
                ", componentId=" + componentId +
                ", componentSecret='" + componentSecret + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
