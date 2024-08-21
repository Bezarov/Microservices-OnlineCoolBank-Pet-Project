package com.example.userscomponent.config;

import com.example.userscomponent.dto.UsersDTO;
import com.example.userscomponent.dto.ErrorDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public ProducerFactory<String, ErrorDTO> usersErrorDTOProducerFactory() {
        Map<String, Object> usersErrorDTOProducerProp = new HashMap<>();
        usersErrorDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        usersErrorDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        usersErrorDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(usersErrorDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, ErrorDTO> usersErrorDTOKafkaTemplate() {
        return new KafkaTemplate<>(usersErrorDTOProducerFactory());
    }

    @Bean
    public ProducerFactory<String, String> StringMessageProduceFactory() {
        Map<String, Object> StringMessageProducerProp = new HashMap<>();
        StringMessageProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        StringMessageProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        StringMessageProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(StringMessageProducerProp);
    }

    @Bean
    public KafkaTemplate<String, String> StringMessageKafkaTemplate() {
        return new KafkaTemplate<>(StringMessageProduceFactory());
    }

}

