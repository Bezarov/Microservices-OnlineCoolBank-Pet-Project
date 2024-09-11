package com.example.accountcomponent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope("singleton")
public class AccountAppComponentConfigDTO {
    @JsonProperty("componentName")
    private String componentName;
    @JsonProperty("componentId")
    private UUID componentId;
    @JsonProperty("componentSecret")
    private String componentSecret;

    private static String jwtToken;

    public AccountAppComponentConfigDTO() {
    }

    public AccountAppComponentConfigDTO(String componentName, UUID componentId, String componentSecret) {
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

    public static String getJwtToken() {
        return jwtToken;
    }

    public static void setJwtToken(String jwtToken) {
        AccountAppComponentConfigDTO.jwtToken = jwtToken;
    }

    @Override
    public String toString() {
        return "AccountAppComponentConfigDTO{" +
                "componentName='" + componentName + '\'' +
                ", componentId=" + componentId +
                ", componentSecret='" + componentSecret + '\'' +
                ", jwtToken='" + jwtToken + '\'' +
                '}';
    }
}
