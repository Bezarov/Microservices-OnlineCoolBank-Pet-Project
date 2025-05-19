package com.example.userscomponent.config;

import com.example.userscomponent.dto.UsersDTO;
import com.example.userscomponent.exception.GlobalKafkaExceptionHandler;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;
import java.util.UUID;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    @Value("${spring.application.name}")
    private String uniqueUsersComponentGroupId;
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    private final GlobalKafkaExceptionHandler globalKafkaExceptionHandler;

    public KafkaConsumerConfig(GlobalKafkaExceptionHandler globalKafkaExceptionHandler) {
        this.globalKafkaExceptionHandler = globalKafkaExceptionHandler;
    }

    @Bean
    public ConsumerFactory<String, UsersDTO> usersDTOConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueUsersComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.userscomponent.dto.UsersDTO",
                JsonDeserializer.TRUSTED_PACKAGES, "*",
                JsonDeserializer.TYPE_MAPPINGS,
                "com.example.apigatewaycomponent.dto.UsersDTO:com.example.userscomponent.dto.UsersDTO"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UsersDTO> usersDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UsersDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(usersDTOConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, UUID> uuidConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueUsersComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.TRUSTED_PACKAGES, "*"
        ));
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
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueUsersComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.TRUSTED_PACKAGES, "*"
        ));
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
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueUsersComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.HashMap",
                JsonDeserializer.TRUSTED_PACKAGES, "*"
        ));
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
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueUsersComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.HashMap",
                JsonDeserializer.TRUSTED_PACKAGES, "*"
        ));
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