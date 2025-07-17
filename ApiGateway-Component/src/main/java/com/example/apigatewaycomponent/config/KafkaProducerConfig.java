package com.example.apigatewaycomponent.config;

import com.example.apigatewaycomponent.dto.AccountDTO;
import com.example.apigatewaycomponent.dto.AccountUpdateRequestDTO;
import com.example.apigatewaycomponent.dto.AuthRequestDTO;
import com.example.apigatewaycomponent.dto.PaymentDTO;
import com.example.apigatewaycomponent.dto.AccountRefillRequestDTO;
import com.example.apigatewaycomponent.dto.UsersDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Bean
    public KafkaTemplate<String, UsersDTO> usersDTOKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, Map<UUID, UsersDTO>> mapUUIDToUsersDTOKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, PaymentDTO> paymentDTOKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, AuthRequestDTO> authRequestDTOKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, UUID> uuidKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, Map<UUID, AccountDTO>> mapUUIDToAccountDTOKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, Map<UUID, String>> mapUUIDToStringKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, Map<UUID, BigDecimal>> mapUUIDToBigDecimalKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, Map<String, BigDecimal>> mapStringToBigDecimalKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, Map<String, String>> mapStringToStringKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, List<Object>> listObjectKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, AccountRefillRequestDTO> accountRefillKafkaTemplate() {
        return new KafkaTemplate<>(buildProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, AccountUpdateRequestDTO> accountUpdateKafkaTemplate() {
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
