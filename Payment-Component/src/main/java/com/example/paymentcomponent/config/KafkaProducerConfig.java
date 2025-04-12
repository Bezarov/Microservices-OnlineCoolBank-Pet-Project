package com.example.paymentcomponent.config;

import com.example.paymentcomponent.dto.AccountDTO;
import com.example.paymentcomponent.dto.ErrorDTO;
import com.example.paymentcomponent.dto.PaymentDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Bean
    ProducerFactory<String, ErrorDTO> paymentErrorDTOProducerFactory() {
        Map<String, Object> paymentErrorDTOProducerProp = new HashMap<>();
        paymentErrorDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        paymentErrorDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        paymentErrorDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(paymentErrorDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, ErrorDTO> paymentErrorDTOKafkaTemplate() {
        return new KafkaTemplate<>(paymentErrorDTOProducerFactory());
    }

    @Bean
    ProducerFactory<String, PaymentDTO> paymentDTOProducerFactory() {
        Map<String, Object> paymentDTOProducerProp = new HashMap<>();
        paymentDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        paymentDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        paymentDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(paymentDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, PaymentDTO> paymentDTOKafkaTemplate() {
        return new KafkaTemplate<>(paymentDTOProducerFactory());
    }

    @Bean
    ProducerFactory<String, AccountDTO> accountDTOProducerFactory() {
        Map<String, Object> accountDTOProducerProp = new HashMap<>();
        accountDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        accountDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        accountDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(accountDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, AccountDTO> accountDTOKafkaTemplate() {
        return new KafkaTemplate<>(accountDTOProducerFactory());
    }

    @Bean
    ProducerFactory<String, List<PaymentDTO>> paymentDTOSProducerFactory() {
        Map<String, Object> listOfPaymentDTOSProducerProp = new HashMap<>();
        listOfPaymentDTOSProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        listOfPaymentDTOSProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        listOfPaymentDTOSProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(listOfPaymentDTOSProducerProp);
    }

    @Bean
    public KafkaTemplate<String, List<PaymentDTO>> paymentDTOSKafkaTemplate() {
        return new KafkaTemplate<>(paymentDTOSProducerFactory());
    }
}
