package com.example.paymentcomponent.config;

import com.example.paymentcomponent.dto.PaymentDTO;
import com.example.paymentcomponent.exception.GlobalKafkaExceptionHandler;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ListDeserializer;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    @Value("${spring.application.name}")
    private String uniquePaymentComponentGroupId;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    private final GlobalKafkaExceptionHandler globalKafkaExceptionHandler;

    public KafkaConsumerConfig(GlobalKafkaExceptionHandler globalKafkaExceptionHandler) {
        this.globalKafkaExceptionHandler = globalKafkaExceptionHandler;
    }

    @Bean
    public ConsumerFactory<String, PaymentDTO> paymentDTOConsumerFactory() {
        Map<String, Object> paymentDTOConsumerProp = new HashMap<>();
        paymentDTOConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        paymentDTOConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, uniquePaymentComponentGroupId);
        paymentDTOConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        paymentDTOConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        paymentDTOConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        paymentDTOConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.paymentcomponent.dto.PaymentDTO");
        paymentDTOConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        paymentDTOConsumerProp.put(JsonDeserializer.TYPE_MAPPINGS,
                "com.example.apigatewaycomponent.dto.PaymentDTO:com.example.paymentcomponent.dto.PaymentDTO");
        return new DefaultKafkaConsumerFactory<>(paymentDTOConsumerProp);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, PaymentDTO> paymentDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentDTOConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, List<PaymentDTO>> listOfPaymentDTOConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, uniquePaymentComponentGroupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, ListDeserializer.class.getName());
        props.put("spring.kafka.value.list.class", PaymentDTO.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, List<PaymentDTO>> paymentListKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, List<PaymentDTO>> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(listOfPaymentDTOConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, UUID> uuidConsumerFactory() {
        Map<String, Object> uuidConsumerProp = new HashMap<>();
        uuidConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        uuidConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, uniquePaymentComponentGroupId);
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
    public ConsumerFactory<String, Map<Object, Object>> mapObjectToObjectConsumerFactory() {
        Map<String, Object> mapObjectToObjectConsumerProp = new HashMap<>();
        mapObjectToObjectConsumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        mapObjectToObjectConsumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, uniquePaymentComponentGroupId);
        mapObjectToObjectConsumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        mapObjectToObjectConsumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        mapObjectToObjectConsumerProp.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        mapObjectToObjectConsumerProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.HashMap");
        mapObjectToObjectConsumerProp.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(mapObjectToObjectConsumerProp);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Map<Object, Object>> mapObjectToObjectKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Map<Object, Object>> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(mapObjectToObjectConsumerFactory());
        factory.setCommonErrorHandler(globalKafkaExceptionHandler);
        return factory;
    }
}
