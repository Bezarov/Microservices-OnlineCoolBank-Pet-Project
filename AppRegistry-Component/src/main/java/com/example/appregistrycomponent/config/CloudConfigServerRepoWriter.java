package com.example.appregistrycomponent.config;

import com.example.appregistrycomponent.model.AppComponent;
import com.example.appregistrycomponent.repository.AppComponentRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;

@Component
public class CloudConfigServerRepoWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudConfigServerRepoWriter.class);
    private static final String DEFAULT_PROPERTIES_CONFIG_PATH = "default-component-config/";

    private final ComponentConfigReader configReader = ComponentConfigReader.readConfig();
    private final AppRegistryComponentConfig appRegistryConfig;
    private final AppComponentRepository appRegistryRepository;
    private final PasswordEncoder passwordEncoder;

    public CloudConfigServerRepoWriter(AppRegistryComponentConfig appRegistryConfig, AppComponentRepository appRegistryRepository, PasswordEncoder passwordEncoder) {
        this.appRegistryConfig = appRegistryConfig;
        this.appRegistryRepository = appRegistryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    void init() {
        List<AppComponent> components = configReader.getComponents();
        components.forEach(component -> {
            if ("AppRegistry-Component-01".equals(component.getComponentName())) {
                appRegistryConfig.setComponentId(component.getComponentId());
                appRegistryConfig.setComponentSecret(component.getComponentSecret());

                component.setComponentSecret(passwordEncoder.encode(component.getComponentSecret()));
                appRegistryRepository.save(component);
                LOGGER.info("Registered component in DB: {}", component.getComponentName());
            }
        });

        components.forEach(this::createOrUpdateComponentFile);
    }

    private void createOrUpdateComponentFile(AppComponent component) {
        String filePath = "AppRegistry-Component/src/main/resources/OnlineCoolBank-config-repo/" +
                component.getComponentName() + ".properties";
        File file = new File(filePath);

        String defaultProperties = getDefaultProperties(component);
        if (defaultProperties == null) {
            LOGGER.error("Error reading default properties for component: {}", component.getComponentName());
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(defaultProperties);
        } catch (IOException exception) {
            LOGGER.error("Error writing to file: {}", filePath);
        }
        LOGGER.info("Repository config file: {} created/updated successfully.", component.getComponentName());
    }

    private String getDefaultProperties(AppComponent component) {
        String defaultFileName = DEFAULT_PROPERTIES_CONFIG_PATH + component.getComponentName()
                .split("-")[0] + "-Component-default.properties";
        ClassPathResource resource = new ClassPathResource(defaultFileName);

        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("server.address"))
                    line = "server.address=" + component.getComponentAddress();
                else if (line.startsWith("server.port"))
                    line = "server.port=" + component.getComponentPort();
                else if (line.startsWith("eureka.instance.appname"))
                    line = "eureka.instance.appname=" + component.getInstanceEurekaName();
                else if (line.startsWith("spring.kafka.bootstrap-servers"))
                    line = "spring.kafka.bootstrap-servers=" + component.getKafkaBootstrapAddresses();

                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException exception) {
            LOGGER.error("Error reading default properties file or it doesn't exists: {}", defaultFileName);
            return null;
        }
        return content.toString();
    }
}
