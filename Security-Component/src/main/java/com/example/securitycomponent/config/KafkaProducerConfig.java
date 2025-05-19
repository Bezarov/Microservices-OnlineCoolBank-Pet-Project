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

import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Bean
    public KafkaTemplate<String, ErrorDTO> usersErrorDTOKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, AuthResponseDTO> usersAuthResponseDTOKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, SecurityContext> securityContextKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    private <T> ProducerFactory<String, T> buildProducerFactory() {
        return new DefaultKafkaProducerFactory<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        ));
    }
}