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

import java.util.List;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Bean
    public KafkaTemplate<String, ErrorDTO> paymentErrorDTOKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, PaymentDTO> paymentDTOKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, AccountDTO> accountDTOKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, List<PaymentDTO>> paymentDTOSKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    private <T> ProducerFactory<String, T> buildProducerFactory() {
        return new DefaultKafkaProducerFactory<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        ));
    }
}
