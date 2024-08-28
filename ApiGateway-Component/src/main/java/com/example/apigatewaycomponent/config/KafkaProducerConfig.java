package com.example.apigatewaycomponent.config;

import com.example.apigatewaycomponent.dto.AccountDTO;
import com.example.apigatewaycomponent.dto.AuthRequestDTO;
import com.example.apigatewaycomponent.dto.PaymentDTO;
import com.example.apigatewaycomponent.dto.UsersDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String KAFKA_BOOTSTRAP_SERVERS;

    public ProducerFactory<String, UsersDTO> usersDTOProducerFactory() {
        Map<String, Object> usersDTOProducerProp = new HashMap<>();
        usersDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        usersDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        usersDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(usersDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, UsersDTO> usersDTOKafkaTemplate() {
        return new KafkaTemplate<>(usersDTOProducerFactory());
    }

    public ProducerFactory<String, PaymentDTO> paymentDTOProducerFactory() {
        Map<String, Object> paymentDTOProducerProp = new HashMap<>();
        paymentDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        paymentDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        paymentDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(paymentDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, PaymentDTO> paymentDTOKafkaTemplate() {
        return new KafkaTemplate<>(paymentDTOProducerFactory());
    }

    public ProducerFactory<String, AuthRequestDTO> authRequestDTOProducerFactory() {
        Map<String, Object> authRequestDTOProducerProp = new HashMap<>();
        authRequestDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        authRequestDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        authRequestDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(authRequestDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, AuthRequestDTO> authRequestDTOKafkaTemplate() {
        return new KafkaTemplate<>(authRequestDTOProducerFactory());
    }

    public ProducerFactory<String, Object> objectProducerFactory() {
        Map<String, Object> usersDTOProducerProp = new HashMap<>();
        usersDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        usersDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        usersDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(usersDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, Object> objectDTOKafkaTemplate() {
        return new KafkaTemplate<>(objectProducerFactory());
    }

    @Bean
    public ProducerFactory<String, UUID> uuidProducerFactory() {
        Map<String, Object> uuidProducerProp = new HashMap<>();
        uuidProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        uuidProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        uuidProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class);
        return new DefaultKafkaProducerFactory<>(uuidProducerProp);
    }

    @Bean
    public KafkaTemplate<String, UUID> uuidKafkaTemplate() {
        return new KafkaTemplate<>(uuidProducerFactory());
    }

    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> stringProducerProp = new HashMap<>();
        stringProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        stringProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        stringProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(stringProducerProp);
    }

    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    @Bean
    public ProducerFactory<String, Map<UUID, UsersDTO>> mapUUIDToUsersDTOProducerFactory() {
        Map<String, Object> mapUUIDToDTOProducerProp = new HashMap<>();
        mapUUIDToDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        mapUUIDToDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        mapUUIDToDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(mapUUIDToDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, Map<UUID, UsersDTO>> mapUUIDToUsersDTOKafkaTemplate() {
        return new KafkaTemplate<>(mapUUIDToUsersDTOProducerFactory());
    }

    @Bean
    public ProducerFactory<String, Map<UUID, AccountDTO>> mapUUIDToAccountDTOProducerFactory() {
        Map<String, Object> mapUUIDToDTOProducerProp = new HashMap<>();
        mapUUIDToDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        mapUUIDToDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        mapUUIDToDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(mapUUIDToDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, Map<UUID, AccountDTO>> mapUUIDToAccountDTOKafkaTemplate() {
        return new KafkaTemplate<>(mapUUIDToAccountDTOProducerFactory());
    }

    @Bean
    public ProducerFactory<String, Map<UUID, String>> mapUUIDToStringProducerFactory() {
        Map<String, Object> mapUUIDToStringProducerProp = new HashMap<>();
        mapUUIDToStringProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        mapUUIDToStringProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        mapUUIDToStringProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(mapUUIDToStringProducerProp);
    }

    @Bean
    public KafkaTemplate<String, Map<UUID, String>> mapUUIDToStringKafkaTemplate() {
        return new KafkaTemplate<>(mapUUIDToStringProducerFactory());
    }

    @Bean
    public ProducerFactory<String, Map<UUID, BigDecimal>> mapUUIDToBigDecimalProducerFactory() {
        Map<String, Object> mapUUIDToStringProducerProp = new HashMap<>();
        mapUUIDToStringProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        mapUUIDToStringProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        mapUUIDToStringProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(mapUUIDToStringProducerProp);
    }

    @Bean
    public KafkaTemplate<String, Map<UUID, BigDecimal>> mapUUIDToBigDecimalKafkaTemplate() {
        return new KafkaTemplate<>(mapUUIDToBigDecimalProducerFactory());
    }

    @Bean
    public ProducerFactory<String, Map<String, BigDecimal>> mapStringToBigDecimalProducerFactory() {
        Map<String, Object> mapUUIDToStringProducerProp = new HashMap<>();
        mapUUIDToStringProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        mapUUIDToStringProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        mapUUIDToStringProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(mapUUIDToStringProducerProp);
    }

    @Bean
    public KafkaTemplate<String, Map<String, BigDecimal>> mapStringToBigDecimalKafkaTemplate() {
        return new KafkaTemplate<>(mapStringToBigDecimalProducerFactory());
    }

    @Bean
    public ProducerFactory<String, Map<String, String>> mapStringToStringProducerFactory() {
        Map<String, Object> mapUUIDToStringProducerProp = new HashMap<>();
        mapUUIDToStringProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        mapUUIDToStringProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        mapUUIDToStringProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(mapUUIDToStringProducerProp);
    }

    @Bean
    public KafkaTemplate<String, Map<String, String >> mapStringToStringKafkaTemplate() {
        return new KafkaTemplate<>(mapStringToStringProducerFactory());
    }

    @Bean
    public ProducerFactory<String, List<Object>> listObjectProducerFactory() {
        Map<String, Object> listObjectProducerProp = new HashMap<>();
        listObjectProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        listObjectProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        listObjectProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(listObjectProducerProp);
    }

    @Bean
    public KafkaTemplate<String, List<Object>> listObjectKafkaTemplate() {
        return new KafkaTemplate<>(listObjectProducerFactory());
    }
}
