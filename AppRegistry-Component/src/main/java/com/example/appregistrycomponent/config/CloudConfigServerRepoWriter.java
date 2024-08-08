package com.example.appregistrycomponent.config;

import com.example.appregistrycomponent.model.AppComponent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;

@Component
public class CloudConfigServerRepoWriter {
    private final ComponentConfigReader configReader = ComponentConfigReader.readConfig();
    private static final Logger logger = LoggerFactory.getLogger(CloudConfigServerRepoWriter.class);


    @PostConstruct
    void init() {
        List<AppComponent> components = configReader.getComponents().stream()
                .filter(component ->
                        "Eureka-Server-01".equals(component.getComponentName()) ||
                        "Users-01".equals(component.getComponentName()) ||
                        "Account-01".equals(component.getComponentName()) ||
                        "Card-01".equals(component.getComponentName()) ||
                        "Payment-01".equals(component.getComponentName()) ||
                        "Auth-01".equals(component.getComponentName()))
                .toList();
        components.forEach(this::updateComponentFile);
    }

    private void updateComponentFile(AppComponent component) {
        String filePath = getFilePathForComponent(component.getComponentName());
        if (filePath == null) {
            logger.error("No file path found for component: " + component.getComponentName());
            return;
        }

        File file = new File(filePath);
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("server.port")) {
                    line = "server.port=" + extractPortFromUrl(component.getComponentURL());
                } else if (line.startsWith("server.address")) {
                    line = "server.address=" + component.getComponentURL();
                }
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            logger.error("Error reading file: " + filePath, e);
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content.toString());
        } catch (IOException e) {
            logger.error("Error writing to file: " + filePath, e);
        }

        logger.info("File {} updated successfully.", filePath);
    }

    private String getFilePathForComponent(String componentName) {
        return switch (componentName) {
            case "Eureka-Server-01" -> "src/main/resources/OnlineCoolBank-config-repo/Eureka-Server-Component-01";
            case "Users-01" -> "src/main/resources/OnlineCoolBank-config-repo/Users-Component-01";
            case "Account-01" -> "src/main/resources/OnlineCoolBank-config-repo/Account-Component-01";
            case "Card-01" -> "src/main/resources/OnlineCoolBank-config-repo/Card-Component-01";
            case "Payment-01" -> "src/main/resources/OnlineCoolBank-config-repo/Payment-Component-01";
            case "Auth-01" -> "src/main/resources/OnlineCoolBank-config-repo/Auth-Component-01";
            default -> null;
        };
    }

    private static int extractPortFromUrl(String url) {
        String[] parts = url.split(":");
        return Integer.parseInt(parts[parts.length - 1].replaceAll("\\D", ""));
    }
}
