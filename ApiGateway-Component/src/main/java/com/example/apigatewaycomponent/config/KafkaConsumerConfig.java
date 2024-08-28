package com.example.apigatewaycomponent.config;

import com.example.apigatewaycomponent.dto.*;
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
    @Value("${spring.kafka.bootstrap-servers}")
    private String KAFKA_BOOTSTRAP_SERVERS;

    @Bean
    public ConsumerFactory<String, ErrorDTO> errorDTOConsumerFactory() {
        Map<String, Object> errorDTOConsumerProp = new HashMap<>();
        errorDTOConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        errorDTOConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_GATEWAY_COMPONENT_GROUP_ID);
        errorDTOConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        errorDTOConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        errorDTOConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        errorDTOConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.apigatewaycomponent.dto.ErrorDTO");
        errorDTOConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        errorDTOConsumerProp.put(JsonDeserializer.TYPE_MAPPINGS,
                "com.example.userscomponent.dto.ErrorDTO:com.example.apigatewaycomponent.dto.ErrorDTO," +
                        "com.example.accountcomponent.dto.ErrorDTO:com.example.apigatewaycomponent.dto.ErrorDTO");
        return new DefaultKafkaConsumerFactory<>(errorDTOConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ErrorDTO> errorDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ErrorDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(errorDTOConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, UsersDTO> usersDTOConsumerFactory() {
        Map<String, Object> usersDTOConsumerProp = new HashMap<>();
        usersDTOConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
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
    public ConsumerFactory<String, AccountDTO> accountDTOConsumerFactory() {
        Map<String, Object> accountDTOConsumerProp = new HashMap<>();
        accountDTOConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        accountDTOConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_GATEWAY_COMPONENT_GROUP_ID);
        accountDTOConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        accountDTOConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        accountDTOConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        accountDTOConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.apigatewaycomponent.dto.AccountDTO");
        accountDTOConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        accountDTOConsumerProp.put(JsonDeserializer.TYPE_MAPPINGS, "com.example.accountcomponent.dto.AccountDTO:" +
                "com.example.apigatewaycomponent.dto.AccountDTO");
        return new DefaultKafkaConsumerFactory<>(accountDTOConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AccountDTO> accountDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AccountDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(accountDTOConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, CardDTO> cardDTOConsumerFactory() {
        Map<String, Object> cardDTOConsumerProp = new HashMap<>();
        cardDTOConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        cardDTOConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_GATEWAY_COMPONENT_GROUP_ID);
        cardDTOConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cardDTOConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        cardDTOConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        cardDTOConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.apigatewaycomponent.dto.CardDTO");
        cardDTOConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        cardDTOConsumerProp.put(JsonDeserializer.TYPE_MAPPINGS, "com.example.cardcomponent.dto.CardDTO:" +
                "com.example.apigatewaycomponent.dto.CardDTO");
        return new DefaultKafkaConsumerFactory<>(cardDTOConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CardDTO> cardDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CardDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cardDTOConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, PaymentDTO> paymentDTOConsumerFactory() {
        Map<String, Object> paymentDTOConsumerProp = new HashMap<>();
        paymentDTOConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        paymentDTOConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_GATEWAY_COMPONENT_GROUP_ID);
        paymentDTOConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        paymentDTOConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        paymentDTOConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        paymentDTOConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.apigatewaycomponent.dto.PaymentDTO");
        paymentDTOConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        paymentDTOConsumerProp.put(JsonDeserializer.TYPE_MAPPINGS, "com.example.paymentcomponent.dto.PaymentDTO:" +
                "com.example.apigatewaycomponent.dto.PaymentDTO");
        return new DefaultKafkaConsumerFactory<>(paymentDTOConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentDTO> paymentDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentDTOConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, AuthResponseDTO> securityDTOConsumerFactory() {
        Map<String, Object> securityDTOConsumerProp = new HashMap<>();
        securityDTOConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        securityDTOConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_GATEWAY_COMPONENT_GROUP_ID);
        securityDTOConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        securityDTOConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        securityDTOConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        securityDTOConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.apigatewaycomponent.dto.AuthResponseDTO");
        securityDTOConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        securityDTOConsumerProp.put(JsonDeserializer.TYPE_MAPPINGS, "com.example.securitycomponent.dto.AuthResponseDTO:" +
                "com.example.apigatewaycomponent.dto.AuthResponseDTO");
        return new DefaultKafkaConsumerFactory<>(securityDTOConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AuthResponseDTO> securityDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AuthResponseDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(securityDTOConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory() {
        Map<String, Object> StringConsumerProp = new HashMap<>();
        StringConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        StringConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_GATEWAY_COMPONENT_GROUP_ID);
        StringConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        StringConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return new DefaultKafkaConsumerFactory<>(StringConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> stringKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stringConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, List<AccountDTO>> listConsumerFactory() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
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