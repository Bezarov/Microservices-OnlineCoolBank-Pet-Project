package com.example.accountcomponent.config;

import com.example.accountcomponent.dto.RefillRequestDTO;
import com.example.accountcomponent.dto.UpdateRequestDTO;
import com.example.accountcomponent.exception.GlobalKafkaExceptionHandler;
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
    private String uniqueAccountComponentGroupId;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    private final GlobalKafkaExceptionHandler globalKafkaExceptionHandler;

    public KafkaConsumerConfig(GlobalKafkaExceptionHandler globalKafkaExceptionHandler) {
        this.globalKafkaExceptionHandler = globalKafkaExceptionHandler;
    }

    @Bean
    public ConsumerFactory<String, UUID> uuidConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueAccountComponentGroupId,
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
                ConsumerConfig.GROUP_ID_CONFIG, uniqueAccountComponentGroupId,
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
    public ConsumerFactory<String, Map<Object, Object>> mapObjectToObjectConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueAccountComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.HashMap",
                JsonDeserializer.TRUSTED_PACKAGES, "*"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Map<Object, Object>> mapObjectToObjectKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Map<Object, Object>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(mapObjectToObjectConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, RefillRequestDTO> refillConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueAccountComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.TYPE_MAPPINGS,
                "com.example.apigatewaycomponent.dto.AccountRefillRequestDTO:com.example.accountcomponent.dto.RefillRequestDTO"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RefillRequestDTO> refillKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, RefillRequestDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(refillConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, UpdateRequestDTO> updateConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueAccountComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.TYPE_MAPPINGS,
                "com.example.apigatewaycomponent.dto.AccountUpdateRequestDTO:com.example.accountcomponent.dto.UpdateRequestDTO"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UpdateRequestDTO> updateKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UpdateRequestDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(updateConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }
}