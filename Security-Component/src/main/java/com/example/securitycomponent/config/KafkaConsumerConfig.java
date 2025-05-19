package com.example.securitycomponent.config;

import com.example.securitycomponent.dto.AuthRequestDTO;
import com.example.securitycomponent.exception.GlobalKafkaExceptionHandler;
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

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    @Value("${spring.application.name}")
    private String uniqueSecurityComponentGroupId;
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    private final GlobalKafkaExceptionHandler globalKafkaExceptionHandler;

    public KafkaConsumerConfig(GlobalKafkaExceptionHandler globalKafkaExceptionHandler) {
        this.globalKafkaExceptionHandler = globalKafkaExceptionHandler;
    }

    @Bean
    public ConsumerFactory<String, AuthRequestDTO> usersAuthRequestDTOConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueSecurityComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.securitycomponent.dto.AuthRequestDTO",
                JsonDeserializer.TRUSTED_PACKAGES, "*",
                JsonDeserializer.TYPE_MAPPINGS,
                "com.example.apigatewaycomponent.dto.AuthRequestDTO:com.example.securitycomponent.dto.AuthRequestDTO"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AuthRequestDTO> usersAuthRequestDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AuthRequestDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(usersAuthRequestDTOConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, Map<String, String>> mapStringToStringConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueSecurityComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.HashMap",
                JsonDeserializer.TRUSTED_PACKAGES, "*"
        ));
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