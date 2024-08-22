package com.example.apigatewaycomponent.config;

import com.example.apigatewaycomponent.dto.UsersDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Object> producerUsersFactory() {
        Map<String, Object> producerProp = new HashMap<>();
        producerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(producerProp);
    }
    @Bean
    public KafkaTemplate<String, Object> kafkaUsersTemplate() {
        return new KafkaTemplate<>(producerUsersFactory());
    }

    public ProducerFactory<String, UsersDTO> usersDTOProducerFactory() {
        Map<String, Object> usersDTOProducerProp = new HashMap<>();
        usersDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        usersDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        usersDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(usersDTOProducerProp);
    }
    @Bean
    public KafkaTemplate<String, UsersDTO> usersDTOKafkaTemplate() {
        return new KafkaTemplate<>(usersDTOProducerFactory());
    }

    @Bean
    public ProducerFactory<String, UUID> uuidProducerFactory() {
        Map<String, Object> uuidProducerProp = new HashMap<>();
        uuidProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        uuidProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        uuidProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class);
        return new DefaultKafkaProducerFactory<>(uuidProducerProp);
    }

    @Bean
    public KafkaTemplate<String, UUID> uuidKafkaTemplate() {
        return new KafkaTemplate<>(uuidProducerFactory());
    }
}
