package com.example.securitycomponent.dto;

import java.util.UUID;

public class AppComponentDTO {
    private UUID componentId;
    private String componentAddress;
    private String componentPort;
    private String componentName;
    private String componentSecret;

    public AppComponentDTO() {
    }

    public AppComponentDTO(UUID componentId, String componentAddress, String componentPort, String componentName, String componentSecret) {
        this.componentId = componentId;
        this.componentAddress = componentAddress;
        this.componentPort = componentPort;
        this.componentName = componentName;
        this.componentSecret = componentSecret;
    }

    public UUID getComponentId() {
        return componentId;
    }

    public void setComponentId(UUID componentId) {
        this.componentId = componentId;
    }

    public String getComponentAddress() {
        return componentAddress;
    }

    public void setComponentAddress(String componentAddress) {
        this.componentAddress = componentAddress;
    }

    public String getComponentPort() {
        return componentPort;
    }

    public void setComponentPort(String componentPort) {
        this.componentPort = componentPort;
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

    @Override
    public String toString() {
        return "AppComponentDTO{" +
                "componentId=" + componentId +
                ", componentAddress='" + componentAddress + '\'' +
                ", componentPort='" + componentPort + '\'' +
                ", componentName='" + componentName + '\'' +
                ", componentSecret='" + componentSecret + '\'' +
                '}';
    }
}
