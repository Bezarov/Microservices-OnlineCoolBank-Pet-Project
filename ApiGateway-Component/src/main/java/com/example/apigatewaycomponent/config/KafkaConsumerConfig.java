package com.example.apigatewaycomponent.config;

import com.example.apigatewaycomponent.deserializer.SecurityContextDeserializer;
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
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.List;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    @Value("${spring.application.name}")
    private String uniqueGatewayComponentGroupId;
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    private static final String DEFAULT_ARRAYLIST_CLASS = "java.util.ArrayList";

    @Bean
    public ConsumerFactory<String, ErrorDTO> errorDTOConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueGatewayComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.apigatewaycomponent.dto.ErrorDTO",
                JsonDeserializer.TRUSTED_PACKAGES, "*",
                JsonDeserializer.TYPE_MAPPINGS,
                "com.example.securitycomponent.dto.ErrorDTO:com.example.apigatewaycomponent.dto.ErrorDTO," +
                        "com.example.userscomponent.dto.ErrorDTO:com.example.apigatewaycomponent.dto.ErrorDTO," +
                        "com.example.accountcomponent.dto.ErrorDTO:com.example.apigatewaycomponent.dto.ErrorDTO," +
                        "com.example.cardcomponent.dto.ErrorDTO:com.example.apigatewaycomponent.dto.ErrorDTO," +
                        "com.example.paymentcomponent.dto.ErrorDTO:com.example.apigatewaycomponent.dto.ErrorDTO"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ErrorDTO> errorDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ErrorDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(errorDTOConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, UsersDTO> usersDTOConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueGatewayComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.apigatewaycomponent.dto.UsersDTO",
                JsonDeserializer.TRUSTED_PACKAGES, "*",
                JsonDeserializer.TYPE_MAPPINGS,
                "com.example.userscomponent.dto.UsersDTO:com.example.apigatewaycomponent.dto.UsersDTO"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UsersDTO> usersDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UsersDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(usersDTOConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, AccountDTO> accountDTOConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueGatewayComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.apigatewaycomponent.dto.AccountDTO",
                JsonDeserializer.TRUSTED_PACKAGES, "*",
                JsonDeserializer.TYPE_MAPPINGS,
                "com.example.accountcomponent.dto.AccountDTO:com.example.apigatewaycomponent.dto.AccountDTO"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AccountDTO> accountDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AccountDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(accountDTOConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, List<AccountDTO>> listAccountDTOKafkaContainerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueGatewayComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.VALUE_DEFAULT_TYPE, DEFAULT_ARRAYLIST_CLASS,
                JsonDeserializer.TRUSTED_PACKAGES, "*"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, List<AccountDTO>> listAccountDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, List<AccountDTO>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(listAccountDTOKafkaContainerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, CardDTO> cardDTOConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueGatewayComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.apigatewaycomponent.dto.CardDTO",
                JsonDeserializer.TRUSTED_PACKAGES, "*",
                JsonDeserializer.TYPE_MAPPINGS,
                "com.example.cardcomponent.dto.CardDTO:com.example.apigatewaycomponent.dto.CardDTO"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CardDTO> cardDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CardDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cardDTOConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, List<CardDTO>> listCardDTOKafkaContainerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueGatewayComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.VALUE_DEFAULT_TYPE, DEFAULT_ARRAYLIST_CLASS,
                JsonDeserializer.TRUSTED_PACKAGES, "*"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, List<CardDTO>> listCardDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, List<CardDTO>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(listCardDTOKafkaContainerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, PaymentDTO> paymentDTOConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueGatewayComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.apigatewaycomponent.dto.PaymentDTO",
                JsonDeserializer.TRUSTED_PACKAGES, "*",
                JsonDeserializer.TYPE_MAPPINGS,
                "com.example.paymentcomponent.dto.PaymentDTO:com.example.apigatewaycomponent.dto.PaymentDTO"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentDTO> paymentDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentDTOConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, List<PaymentDTO>> listPaymentDTOKafkaContainerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueGatewayComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.VALUE_DEFAULT_TYPE, DEFAULT_ARRAYLIST_CLASS,
                JsonDeserializer.TRUSTED_PACKAGES, "*"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, List<PaymentDTO>> listPaymentDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, List<PaymentDTO>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(listPaymentDTOKafkaContainerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, AuthResponseDTO> securityDTOConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueGatewayComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.apigatewaycomponent.dto.AuthResponseDTO",
                JsonDeserializer.TRUSTED_PACKAGES, "*",
                JsonDeserializer.TYPE_MAPPINGS,
                "com.example.securitycomponent.dto.AuthResponseDTO:com.example.apigatewaycomponent.dto.AuthResponseDTO"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AuthResponseDTO> securityDTOKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AuthResponseDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(securityDTOConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, uniqueGatewayComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName()
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> stringKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stringConsumerFactory());
        return factory;
    }


    @Bean
    public ConsumerFactory<String, SecurityContextImpl> securityContextConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest",
                ConsumerConfig.GROUP_ID_CONFIG, uniqueGatewayComponentGroupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SecurityContextDeserializer.class,
                JsonDeserializer.TRUSTED_PACKAGES, "*"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SecurityContextImpl> securityContextKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SecurityContextImpl> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(securityContextConsumerFactory());
        return factory;
    }
}