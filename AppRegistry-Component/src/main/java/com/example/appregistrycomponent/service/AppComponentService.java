package com.example.appregistrycomponent.service;

import com.example.appregistrycomponent.model.AppComponent;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface AppComponentService {
    AppComponent registerComponent(AppComponent appComponent);

    AppComponent getComponentById(UUID componentId);

    AppComponent getComponentByName(String componentName);

    ResponseEntity<String> deleteById (UUID componentId);
}
