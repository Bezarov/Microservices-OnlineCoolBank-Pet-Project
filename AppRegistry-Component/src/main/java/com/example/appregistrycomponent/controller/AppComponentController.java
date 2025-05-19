package com.example.appregistrycomponent.controller;

import com.example.appregistrycomponent.model.AppComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.appregistrycomponent.service.AppComponentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/component")
public class AppComponentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppComponentController.class);
    private static final String RESPONSE_LOG = "Request was successfully processed and response was sent: {}";

    private final AppComponentService appComponentService;

    public AppComponentController(AppComponentService appComponentService) {
        this.appComponentService = appComponentService;
    }

    @PostMapping
    public ResponseEntity<AppComponent> registerComponent(@RequestBody AppComponent appComponent) {
        LOGGER.debug("Received POST request to register Component: {}", appComponent);
        AppComponent responseAppComponent = appComponentService.registerComponent(appComponent);
        LOGGER.debug(RESPONSE_LOG, responseAppComponent);
        return ResponseEntity.ok(responseAppComponent);
    }

    @GetMapping("/by-id/{componentId}")
    public ResponseEntity<AppComponent> getComponentById(@PathVariable UUID componentId) {
        LOGGER.debug("Received GET request to get Component by ID: {}", componentId);
        AppComponent responseAppComponent = appComponentService.getComponentById(componentId);
        LOGGER.debug(RESPONSE_LOG, responseAppComponent);
        return ResponseEntity.ok(responseAppComponent);
    }

    @GetMapping("/by-name/{componentName}")
    public ResponseEntity<AppComponent> getComponentByName(@PathVariable String componentName) {
        LOGGER.debug("Received GET request to get Component by Name: {}", componentName);
        AppComponent responseAppComponent = appComponentService.getComponentByName(componentName);
        LOGGER.debug(RESPONSE_LOG, responseAppComponent);
        return ResponseEntity.ok(responseAppComponent);
    }

    @DeleteMapping("/by-id/{componentId}")
    public ResponseEntity<String> deleteById(@PathVariable UUID componentId){
        LOGGER.debug("Received DELETE request to remove Component by id: {}", componentId);
        ResponseEntity<String> responseMessage = appComponentService.deleteById(componentId);
        LOGGER.debug(RESPONSE_LOG, responseMessage);
        return responseMessage;
    }
}
