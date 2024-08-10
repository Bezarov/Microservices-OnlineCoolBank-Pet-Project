package com.example.appregistrycomponent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class AppComponent {
    @Id
    @JsonProperty("componentId")
    private UUID componentId;

    @JsonProperty("componentAddress")
    private String componentAddress;

    @JsonProperty("componentPort")
    private String componentPort;

    @JsonProperty("instanceEurekaName")
    private String instanceEurekaName;
    @JsonProperty("componentName")
    private String componentName;
    @JsonProperty("componentSecret")
    private String componentSecret;

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

    public String getInstanceEurekaName() {
        return instanceEurekaName;
    }

    public void setInstanceEurekaName(String instanceEurekaName) {
        this.instanceEurekaName = instanceEurekaName;
    }

    @Override
    public String toString() {
        return "AppComponent{" +
                "componentName='" + componentName +
                ", componentId=" + componentId + '\'' +
                ", componentAddress='" + componentAddress + '\'' +
                ", componentPort='" + componentPort + '\'' +
                ", instanceEurekaName='" + instanceEurekaName + '\'' +
                ", componentSecret='" + componentSecret + '\'' +
                '}';
    }
}
