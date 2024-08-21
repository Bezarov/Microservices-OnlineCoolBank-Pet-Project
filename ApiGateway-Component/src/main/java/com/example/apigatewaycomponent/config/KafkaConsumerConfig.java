package com.example.apigatewaycomponent.config;

import com.example.apigatewaycomponent.dto.AccountDTO;
import com.example.apigatewaycomponent.dto.ErrorDTO;
import com.example.apigatewaycomponent.dto.UsersDTO;
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
import java.util.List;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    @Value("${spring.application.name}")
    private String UNIQUE_GATEWAY_COMPONENT_GROUP_ID;

    @Bean
    public ConsumerFactory<String, UsersDTO> usersDTOConsumerFactory() {
        Map<String, Object> usersDTOConsumerProp = new HashMap<>();
        usersDTOConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        usersDTOConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_GATEWAY_COMPONENT_GROUP_ID);
        usersDTOConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        usersDTOConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        usersDTOConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        usersDTOConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.apigatewaycomponent.dto.UsersDTO");
        usersDTOConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        usersDTOConsumerProp.put(JsonDeserializer.TYPE_MAPPINGS, "com.example.userscomponent.dto.UsersDTO:" +
                "com.example.apigatewaycomponent.dto.UsersDTO");
        return new DefaultKafkaConsumerFactory<>(usersDTOConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UsersDTO> usersDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UsersDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(usersDTOConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ErrorDTO> errorDTOConsumerFactory() {
        Map<String, Object> errorDTOConsumerProp = new HashMap<>();
        errorDTOConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        errorDTOConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_GATEWAY_COMPONENT_GROUP_ID);
        errorDTOConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        errorDTOConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        errorDTOConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        errorDTOConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.apigatewaycomponent.dto.ErrorDTO");
        errorDTOConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        errorDTOConsumerProp.put(JsonDeserializer.TYPE_MAPPINGS, "com.example.userscomponent.dto.ErrorDTO:" +
                "com.example.apigatewaycomponent.dto.ErrorDTO");
        return new DefaultKafkaConsumerFactory<>(errorDTOConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ErrorDTO> errorDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ErrorDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(errorDTOConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> StringConsumerFactory() {
        Map<String, Object> StringConsumerProp = new HashMap<>();
        StringConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        StringConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_GATEWAY_COMPONENT_GROUP_ID);
        StringConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        StringConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return new DefaultKafkaConsumerFactory<>(StringConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> StringKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(StringConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, List<AccountDTO>> listConsumerFactory() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_GATEWAY_COMPONENT_GROUP_ID);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        consumerProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.ArrayList");
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(consumerProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, List<AccountDTO>> listKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, List<AccountDTO>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(listConsumerFactory());
        return factory;
    }
}