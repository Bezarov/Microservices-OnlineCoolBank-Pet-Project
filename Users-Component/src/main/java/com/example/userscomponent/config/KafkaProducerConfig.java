package com.example.userscomponent.config;

import com.example.userscomponent.dto.UsersDTO;
import com.example.userscomponent.dto.UsersErrorDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
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
    public ProducerFactory<String, UsersErrorDTO> usersErrorDTOProducerFactory() {
        Map<String, Object> usersErrorDTOProducerProp = new HashMap<>();
        usersErrorDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        usersErrorDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        usersErrorDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(usersErrorDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, UsersErrorDTO> usersErrorDTOKafkaTemplate() {
        return new KafkaTemplate<>(usersErrorDTOProducerFactory());
    }

    @Bean
    public ProducerFactory<String, ResponseEntity<String>> usersDeleteMessageProduceFactory() {
        Map<String, Object> usersDeleteMessageProducerProp = new HashMap<>();
        usersDeleteMessageProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        usersDeleteMessageProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        usersDeleteMessageProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(usersDeleteMessageProducerProp);
    }

    @Bean
    public KafkaTemplate<String, ResponseEntity<String>> usersDeleteMessageKafkaTemplate() {
        return new KafkaTemplate<>(usersDeleteMessageProduceFactory());
    }

}

