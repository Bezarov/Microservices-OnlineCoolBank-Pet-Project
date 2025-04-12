package com.example.securitycomponent.config;

import com.example.securitycomponent.dto.AuthResponseDTO;
import com.example.securitycomponent.dto.ErrorDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.security.core.context.SecurityContext;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Bean
    public ProducerFactory<String, ErrorDTO> usersErrorDTOProducerFactory() {
        Map<String, Object> usersErrorDTOProducerProp = new HashMap<>();
        usersErrorDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        usersErrorDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        usersErrorDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(usersErrorDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, ErrorDTO> usersErrorDTOKafkaTemplate() {
        return new KafkaTemplate<>(usersErrorDTOProducerFactory());
    }

    @Bean
    public ProducerFactory<String, AuthResponseDTO> usersAuthResponseDTOProducerFactory() {
        Map<String, Object> authResponseDTOProducerProp = new HashMap<>();
        authResponseDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        authResponseDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        authResponseDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(authResponseDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, AuthResponseDTO> usersAuthResponseDTOKafkaTemplate() {
        return new KafkaTemplate<>(usersAuthResponseDTOProducerFactory());
    }

    @Bean
    public ProducerFactory<String, SecurityContext> securityContextProducerFactory() {
        Map<String, Object> securityContextProducerProp = new HashMap<>();
        securityContextProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        securityContextProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        securityContextProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(securityContextProducerProp);
    }

    @Bean
    public KafkaTemplate<String, SecurityContext> securityContextKafkaTemplate() {
        return new KafkaTemplate<>(securityContextProducerFactory());
    }
}