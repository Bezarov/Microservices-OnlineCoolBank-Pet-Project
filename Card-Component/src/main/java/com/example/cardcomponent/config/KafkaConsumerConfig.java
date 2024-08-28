package com.example.cardcomponent.config;

import com.example.cardcomponent.dto.CardDTO;
import com.example.cardcomponent.exception.GlobalKafkaExceptionHandler;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    @Value("${spring.application.name}")
    private String UNIQUE_CARD_COMPONENT_GROUP_ID;

    @Value("${spring.kafka.bootstrap-servers}")
    private String KAFKA_BOOTSTRAP_SERVERS;

    private final GlobalKafkaExceptionHandler globalKafkaExceptionHandler;

    public KafkaConsumerConfig(GlobalKafkaExceptionHandler globalKafkaExceptionHandler) {
        this.globalKafkaExceptionHandler = globalKafkaExceptionHandler;
    }

    @Bean
    public ConsumerFactory<String, CardDTO> cardDTOConsumerFactory() {
        Map<String, Object> cardDTOConsumerProp = new HashMap<>();
        cardDTOConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        cardDTOConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_CARD_COMPONENT_GROUP_ID);
        cardDTOConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cardDTOConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        cardDTOConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        cardDTOConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.cardcomponent.dto.CardDTO");
        cardDTOConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        cardDTOConsumerProp.put(JsonDeserializer.TYPE_MAPPINGS,
                "com.example.apigatewaycomponent.dto.CardDTO:com.example.cardcomponent.dto.CardDTO");
        return new DefaultKafkaConsumerFactory<>(cardDTOConsumerProp);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, CardDTO> cardDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CardDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cardDTOConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, UUID> uuidConsumerFactory() {
        Map<String, Object> uuidConsumerProp = new HashMap<>();
        uuidConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        uuidConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_CARD_COMPONENT_GROUP_ID);
        uuidConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        uuidConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        uuidConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, UUIDDeserializer.class.getName());
        uuidConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(uuidConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UUID> uuidKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UUID> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(uuidConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory() {
        Map<String, Object> stringConsumerProp = new HashMap<>();
        stringConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        stringConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_CARD_COMPONENT_GROUP_ID);
        stringConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        stringConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        stringConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class.getName());
        stringConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(stringConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> stringKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stringConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, Map<UUID, String>> mapUUIDToStringConsumerFactory() {
        Map<String, Object> mapUUIDToStringConsumerProp = new HashMap<>();
        mapUUIDToStringConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        mapUUIDToStringConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_CARD_COMPONENT_GROUP_ID);
        mapUUIDToStringConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        mapUUIDToStringConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        mapUUIDToStringConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        mapUUIDToStringConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.HashMap");
        mapUUIDToStringConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(mapUUIDToStringConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Map<UUID, String>> mapUUIDToStringKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Map<UUID, String>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(mapUUIDToStringConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, Map<String, String>> mapStringToStringConsumerFactory() {
        Map<String, Object> mapStringToStringConsumerProp = new HashMap<>();
        mapStringToStringConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        mapStringToStringConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_CARD_COMPONENT_GROUP_ID);
        mapStringToStringConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        mapStringToStringConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        mapStringToStringConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        mapStringToStringConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.HashMap");
        mapStringToStringConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(mapStringToStringConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Map<String, String>> mapStringToStringKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Map<String, String>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(mapStringToStringConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }
}
