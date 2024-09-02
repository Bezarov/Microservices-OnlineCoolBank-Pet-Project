package com.example.appregistrycomponent.service;

import com.example.appregistrycomponent.config.ComponentConfigReader;
import com.example.appregistrycomponent.model.AppComponent;
import com.example.appregistrycomponent.repository.AppComponentRepository;
import com.example.appregistrycomponent.config.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;
import java.util.UUID;

@Service
public class AppComponentServiceImpl implements AppComponentService {
    private static final Logger logger = LoggerFactory.getLogger(AppComponentServiceImpl.class);
    private final AppComponentRepository appComponentRepository;
    private final SecurityConfig security;

    public AppComponentServiceImpl(AppComponentRepository appComponentRepository, SecurityConfig security) {
        this.appComponentRepository = appComponentRepository;
        this.security = security;
    }

    @Override
    public AppComponent registerComponent(AppComponent appComponent) {
        logger.info("Trying to find Component with ID: {}", appComponent.getComponentId());
        appComponentRepository.findById(appComponent.getComponentId())
                .ifPresent(AppComponentEntity -> {
                    logger.error("Component with such ID already registered: {}", appComponent.getComponentId());
                    throw new ResponseStatusException(HttpStatus.FOUND,
                            "Component with such ID: " + appComponent.getComponentId() + " already registered");
                });
        logger.info("Component ID is unique");

        List<AppComponent> components = ComponentConfigReader.readConfig().getComponents();

        logger.info("Comparing received data from request with configured data in component-config.yaml");
        AppComponent matchedIncomingComponentInComponentConfig = components.stream()
                .filter(component -> component.getComponentName().equals(appComponent.getComponentName()) &&
                        component.getComponentId().toString().equals(appComponent.getComponentId().toString()) &&
                        component.getComponentSecret().equals(appComponent.getComponentSecret()))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Received data was not found in global-app-components-config.yml: {}", appComponent);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Invalid Component Credentials: " + appComponent);
                });

        logger.info("Encrypting received component secret before save it in DB");
        matchedIncomingComponentInComponentConfig.setComponentSecret(security.passwordEncoder()
                .encode(matchedIncomingComponentInComponentConfig.getComponentSecret()));

        logger.info("Component registered successfully: {}", matchedIncomingComponentInComponentConfig);
        return appComponentRepository.save(matchedIncomingComponentInComponentConfig);
    }

    @Override
    public AppComponent getComponentById(UUID componentId) {
        logger.info("Trying to find Component with ID: {}", componentId);
        return appComponentRepository.findById(componentId)
                .map(AppComponentEntity -> {
                    logger.info("Component was found and received to the Controller: {}", AppComponentEntity);
                    return AppComponentEntity;
                })
                .orElseThrow(() -> {
                    logger.error("Component with such ID was not found: {}", componentId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Component with such ID:" + componentId + " was not found");
                });
    }

    @Override
    public AppComponent getComponentByName(String componentName) {
        logger.info("Trying to find Component with name: {}", componentName);
        return appComponentRepository.findServiceByComponentName(componentName)
                .map(AppComponentEntity -> {
                    logger.info("Component was found and received to the Controller: {}", AppComponentEntity);
                    return AppComponentEntity;
                })
                .orElseThrow(() -> {
                    logger.error("Component with such name was not found: {}", componentName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Component with such name: " + componentName + " was not found");
                });
    }

    @Override
    public ResponseEntity<String> deleteById(UUID componentId) {
        logger.info("Trying to find Component with id: {}", componentId);
        AppComponent appComponent = appComponentRepository.findById(componentId)
                .orElseThrow(() -> {
                    logger.error("Component with such ID was not found: {}", componentId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Component with such ID:" + componentId + " was not found");
                });
        appComponentRepository.deleteById(componentId);
        logger.info("Component was found and deleted successfully: {}", appComponent);
        return new ResponseEntity<>("Component: " + appComponent.getComponentName() + " deregistered successfully", HttpStatus.ACCEPTED);
    }
}