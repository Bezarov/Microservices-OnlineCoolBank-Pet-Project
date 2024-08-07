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
    @JsonProperty("componentURL")
    private String componentURL;
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

    public String getComponentURL() {
        return componentURL;
    }

    public void setComponentURL(String componentURL) {
        this.componentURL = componentURL;
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
        return "AppComponent{" +
                "componentId=" + componentId +
                ", componentURL='" + componentURL + '\'' +
                ", componentName='" + componentName + '\'' +
                ", componentSecret='" + componentSecret + '\'' +
                '}';
    }
}
