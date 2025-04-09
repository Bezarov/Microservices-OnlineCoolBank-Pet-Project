package com.example.appregistrycomponent.config;

import com.example.appregistrycomponent.model.AppComponent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ComponentConfigReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentConfigReader.class);
    @JsonProperty("components")
    List<AppComponent> components;

    public List<AppComponent> getComponents() {
        return components;
    }

    @Override
    public String toString() {
        return "ComponentConfigReader{" +
                "components=" + components +
                '}';
    }

    public static ComponentConfigReader readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        ComponentConfigReader config = null;
        try {
            LOGGER.debug("Trying to read and deserialize: global-app-components-config.yml file");
            config = objectMapper.readValue(new File(
                    "AppRegistry-Component/src/main/resources/global-app-components-config.yml"),
                    ComponentConfigReader.class);
        } catch (IOException e) {
            LOGGER.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        config.getComponents().forEach(component -> LOGGER.info("Deserialization successfully: {}.", component));
        return config;
    }
}
