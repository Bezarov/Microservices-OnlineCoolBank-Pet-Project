package com.example.apigatewaycomponent.config;

import com.example.apigatewaycomponent.dto.UsersDTO;
import com.example.apigatewaycomponent.errordto.UsersErrorDTO;
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
    public ConsumerFactory<String, UsersErrorDTO> usersErrorDTOConsumerFactory() {
        Map<String, Object> usersErrorDTOConsumerProp = new HashMap<>();
        usersErrorDTOConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        usersErrorDTOConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_GATEWAY_COMPONENT_GROUP_ID);
        usersErrorDTOConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        usersErrorDTOConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        usersErrorDTOConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        usersErrorDTOConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.apigatewaycomponent.errordto.UsersErrorDTO");
        usersErrorDTOConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        usersErrorDTOConsumerProp.put(JsonDeserializer.TYPE_MAPPINGS, "com.example.userscomponent.dto.UsersErrorDTO:" +
                "com.example.apigatewaycomponent.errordto.UsersErrorDTO");
        return new DefaultKafkaConsumerFactory<>(usersErrorDTOConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UsersErrorDTO> usersErrorDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UsersErrorDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(usersErrorDTOConsumerFactory());
        return factory;
    }
}