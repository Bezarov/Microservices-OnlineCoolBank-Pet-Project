package com.example.userscomponent.config;

import com.example.userscomponent.dto.UsersDTO;
import com.example.userscomponent.dto.ErrorDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
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
    public ProducerFactory<String, UsersDTO> usersDTOProducerFactory() {
        Map<String, Object> usersDTOProducerProp = new HashMap<>();
        usersDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        usersDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        usersDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(usersDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, UsersDTO> usersDTOKafkaTemplate() {
        return new KafkaTemplate<>(usersDTOProducerFactory());
    }

    @Bean
    public ProducerFactory<String, String> stringMessageProduceFactory() {
        Map<String, Object> stringMessageProducerProp = new HashMap<>();
        stringMessageProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        stringMessageProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        stringMessageProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(stringMessageProducerProp);
    }

    @Bean
    public KafkaTemplate<String, String> stringMessageKafkaTemplate() {
        return new KafkaTemplate<>(stringMessageProduceFactory());
    }
}