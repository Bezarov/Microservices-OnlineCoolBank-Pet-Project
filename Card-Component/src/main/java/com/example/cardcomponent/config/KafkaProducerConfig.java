package com.example.cardcomponent.config;

import com.example.cardcomponent.dto.CardDTO;
import com.example.cardcomponent.dto.ErrorDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ListSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Bean
    ProducerFactory<String, ErrorDTO> cardErrorDTOProducerFactory() {
        Map<String, Object> cardErrorDTOProducerProp = new HashMap<>();
        cardErrorDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        cardErrorDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        cardErrorDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(cardErrorDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, ErrorDTO> cardErrorDTOKafkaTemplate() {
        return new KafkaTemplate<>(cardErrorDTOProducerFactory());
    }

    @Bean
    ProducerFactory<String, CardDTO> cardDTOProducerFactory() {
        Map<String, Object> cardDTOProducerProp = new HashMap<>();
        cardDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        cardDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        cardDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(cardDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, CardDTO> cardDTOKafkaTemplate() {
        return new KafkaTemplate<>(cardDTOProducerFactory());
    }

    @Bean
    ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> stringProducerProp = new HashMap<>();
        stringProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        stringProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        stringProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(stringProducerProp);

    }

    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    @Bean
    ProducerFactory<String, List<CardDTO>> listOfDTOSProducerFactory() {
        Map<String, Object> listOfDTOSProducerProp = new HashMap<>();
        listOfDTOSProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        listOfDTOSProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        listOfDTOSProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ListSerializer.class);
        return new DefaultKafkaProducerFactory<>(listOfDTOSProducerProp);

    }

    @Bean
    public KafkaTemplate<String, List<CardDTO>> listOfDTOSKafkaTemplate() {
        return new KafkaTemplate<>(listOfDTOSProducerFactory());
    }
}
