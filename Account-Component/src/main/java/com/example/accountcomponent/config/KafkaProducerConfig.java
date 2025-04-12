package com.example.accountcomponent.config;

import com.example.accountcomponent.dto.AccountDTO;
import com.example.accountcomponent.dto.ErrorDTO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Bean
    ProducerFactory<String, ErrorDTO> accountErrorDTOProducerFactory() {
        Map<String, Object> accountErrorDTOProducerProp = new HashMap<>();
        accountErrorDTOProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        accountErrorDTOProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        accountErrorDTOProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(accountErrorDTOProducerProp);
    }

    @Bean
    public KafkaTemplate<String, ErrorDTO> accountErrorDTOKafkaTemplate() {
        return new KafkaTemplate<>(accountErrorDTOProducerFactory());
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
    ProducerFactory<String, BigDecimal> bigDecimalProducerFactory() {
        Map<String, Object> bigDecimalProducerProp = new HashMap<>();
        bigDecimalProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        bigDecimalProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        bigDecimalProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(bigDecimalProducerProp);
    }

    @Bean
    public KafkaTemplate<String, BigDecimal> bigDecimalKafkaTemplate() {
        return new KafkaTemplate<>(bigDecimalProducerFactory());
    }

    @Bean
    ProducerFactory<String, List<AccountDTO>> accountDTOSProducerFactory() {
        Map<String, Object> listOfAccountDTOSProducerProp = new HashMap<>();
        listOfAccountDTOSProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        listOfAccountDTOSProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        listOfAccountDTOSProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(listOfAccountDTOSProducerProp);
    }

    @Bean
    public KafkaTemplate<String, List<AccountDTO>> accountDTOSKafkaTemplate() {
        return new KafkaTemplate<>(accountDTOSProducerFactory());
    }

    @Bean
    ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> stringProducerProp = new HashMap<>();
        stringProducerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        stringProducerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        stringProducerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(stringProducerProp);

    }

    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }
}
