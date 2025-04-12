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

import java.util.HashMap;
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
        Map<String, Object> usersAuthRequestDTOConsumerProp = new HashMap<>();
        usersAuthRequestDTOConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        usersAuthRequestDTOConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, uniqueSecurityComponentGroupId);
        usersAuthRequestDTOConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        usersAuthRequestDTOConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        usersAuthRequestDTOConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        usersAuthRequestDTOConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.securitycomponent.dto.AuthRequestDTO");
        usersAuthRequestDTOConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        usersAuthRequestDTOConsumerProp.put(JsonDeserializer.TYPE_MAPPINGS,
                "com.example.apigatewaycomponent.dto.AuthRequestDTO:com.example.securitycomponent.dto.AuthRequestDTO");
        return new DefaultKafkaConsumerFactory<>(usersAuthRequestDTOConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AuthRequestDTO> usersAuthRequestDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AuthRequestDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(usersAuthRequestDTOConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, Map<String, String>> mapStringToStringConsumerFactory() {
        Map<String, Object> mapStringToStringConsumerProp = new HashMap<>();
        mapStringToStringConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        mapStringToStringConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, uniqueSecurityComponentGroupId);
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