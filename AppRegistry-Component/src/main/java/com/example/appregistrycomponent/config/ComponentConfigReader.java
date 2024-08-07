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
    private static final Logger logger = LoggerFactory.getLogger(ComponentConfigReader.class);
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
            logger.info("Trying to read and deserialize component-config.yml file");
            config = objectMapper.readValue(new File(
                    "src/main/resources/component-config.yml"), ComponentConfigReader.class);
        } catch (IOException e) {
            logger.error("Error: File cannot be found or its contents cannot be deserialized");
            e.printStackTrace();
        }
        logger.debug("Deserialization successfully: {}", config);
        return config;
    }
}
