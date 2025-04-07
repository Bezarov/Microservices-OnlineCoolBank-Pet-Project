package com.example.apigatewaycomponent.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;

import java.io.IOException;

public class SecurityContextDeserializer implements Deserializer<SecurityContextImpl> {
    private static final Logger logger = LoggerFactory.getLogger(SecurityContextDeserializer.class);
    private final ObjectMapper objectMapper;

    public SecurityContextDeserializer() {
        this.objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Authentication.class, new AuthenticationDeserializer());
        objectMapper.registerModule(module);
    }

    @Override
    public SecurityContextImpl deserialize(String topic, byte[] data) {
        if (data == null)
            return null;
        try {
            return objectMapper.readValue(data, SecurityContextImpl.class);
        } catch (IOException exception) {
            logger.error("Error during deserialization SecurityContextImpl");
            throw new RuntimeException("Error during deserialization SecurityContextImpl", exception);
        }
    }
}