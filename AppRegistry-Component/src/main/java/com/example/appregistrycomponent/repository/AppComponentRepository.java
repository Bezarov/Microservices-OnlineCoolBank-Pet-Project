package com.example.appregistrycomponent.repository;

import com.example.appregistrycomponent.model.AppComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppComponentRepository extends JpaRepository<AppComponent, UUID> {
    Optional<AppComponent> findServiceByComponentName(String serviceName);
}
