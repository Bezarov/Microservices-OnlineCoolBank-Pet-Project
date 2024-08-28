package com.example.userscomponent.config;

import com.example.userscomponent.dto.UsersDTO;
import com.example.userscomponent.exception.GlobalKafkaExceptionHandler;
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
    private String UNIQUE_USERS_COMPONENT_GROUP_ID;
    @Value("${spring.kafka.bootstrap-servers}")
    private String KAFKA_BOOTSTRAP_SERVERS;

    private final GlobalKafkaExceptionHandler globalKafkaExceptionHandler;

    public KafkaConsumerConfig(GlobalKafkaExceptionHandler globalKafkaExceptionHandler) {
        this.globalKafkaExceptionHandler = globalKafkaExceptionHandler;
    }

    @Bean
    public ConsumerFactory<String, UsersDTO> usersDTOConsumerFactory() {
        Map<String, Object> usersDTOConsumerProp = new HashMap<>();
        usersDTOConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        usersDTOConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_USERS_COMPONENT_GROUP_ID);
        usersDTOConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        usersDTOConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        usersDTOConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        usersDTOConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.userscomponent.dto.UsersDTO");
        usersDTOConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        usersDTOConsumerProp.put(JsonDeserializer.TYPE_MAPPINGS,
                "com.example.apigatewaycomponent.dto.UsersDTO:com.example.userscomponent.dto.UsersDTO");
        return new DefaultKafkaConsumerFactory<>(usersDTOConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UsersDTO> usersDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UsersDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(usersDTOConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, UUID> uuidConsumerFactory() {
        Map<String, Object> uuidConsumerProp = new HashMap<>();
        uuidConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        uuidConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_USERS_COMPONENT_GROUP_ID);
        uuidConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        uuidConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        uuidConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, UUIDDeserializer.class.getName());
        uuidConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(uuidConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UUID> uuidKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UUID> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(uuidConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory() {
        Map<String, Object> stringConsumerProp = new HashMap<>();
        stringConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        stringConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_USERS_COMPONENT_GROUP_ID);
        stringConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        stringConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        stringConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class.getName());
        stringConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(stringConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> stringKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stringConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, Map<UUID, UsersDTO>> mapUUIDToDTOConsumerFactory() {
        Map<String, Object> mapUUIDToDTOConsumerProp = new HashMap<>();
        mapUUIDToDTOConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        mapUUIDToDTOConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_USERS_COMPONENT_GROUP_ID);
        mapUUIDToDTOConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        mapUUIDToDTOConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        mapUUIDToDTOConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        mapUUIDToDTOConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.HashMap");
        mapUUIDToDTOConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(mapUUIDToDTOConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Map<UUID, UsersDTO>> mapUUIDToDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Map<UUID, UsersDTO>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(mapUUIDToDTOConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, Map<UUID, String>> mapUUIDToStringConsumerFactory() {
        Map<String, Object> mapUUIDToStringConsumerProp = new HashMap<>();
        mapUUIDToStringConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        mapUUIDToStringConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_USERS_COMPONENT_GROUP_ID);
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
}