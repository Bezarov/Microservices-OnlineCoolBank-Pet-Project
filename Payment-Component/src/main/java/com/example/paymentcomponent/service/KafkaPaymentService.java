package com.example.paymentcomponent.service;

import com.example.paymentcomponent.dto.PaymentDTO;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface KafkaPaymentService {
    void createPaymentByAccounts(PaymentDTO paymentDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void createPaymentByCards(String fromCardNumber, String toCardNumber, BigDecimal amount, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getPaymentById(UUID paymentId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getAllAccountPaymentsByFromAccount(UUID fromAccountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getPaymentsByStatus(Map<String, String> mapFromAccountUUIDToStatus, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getAllAccountPaymentsByToAccount(UUID toAccountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getAllAccountPaymentsByPaymentType(Map<String, String> mapFromAccountIdToPaymentType, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getAllFromAccountPaymentsByPaymentDateRange(List<Object> listOfFromAccountIdFromPaymentDateToPaymentDate,
                                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getAllToAccountPaymentsByPaymentDateRange(List<Object> listOfToAccountIdFromPaymentDateToPaymentDate,
                                                   @Header(KafkaHeaders.CORRELATION_ID) String correlationId);
}
