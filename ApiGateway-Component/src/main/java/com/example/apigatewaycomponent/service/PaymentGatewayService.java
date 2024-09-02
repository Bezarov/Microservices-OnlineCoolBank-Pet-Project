package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.ErrorDTO;
import com.example.apigatewaycomponent.dto.PaymentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PaymentGatewayService {
    void handlePaymentErrors(ErrorDTO paymentErrorDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> createPaymentByAccounts(PaymentDTO paymentDTO);

    void handlePaymentCreationByAccountsResponse(PaymentDTO paymentDTO,
                                                 @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> createPaymentByCards(String fromCardNumber,
                                                                   String toCardNumber, BigDecimal amount);

    void handlePaymentCreationByCardsResponse(PaymentDTO paymentDTO,
                                              @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getPaymentById(UUID paymentId);

    void handleGetPaymentByIdResponse(PaymentDTO paymentDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllAccountPaymentsByFromAccount(UUID fromAccountId);

    void handleGetAllPaymentByFromAccountResponse(List<PaymentDTO> paymentDTOS,
                                                  @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getPaymentsByStatus(UUID fromAccountId, String status);

    void handleGetAllPaymentByStatusResponse(List<PaymentDTO> paymentDTOS,
                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllAccountPaymentsByToAccount(UUID toAccountId);

    void handleGetAllPaymentByToAccountResponse(List<PaymentDTO> paymentDTOS,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllAccountPaymentsByPaymentType(UUID fromAccountId,
                                                                                       String paymentType);

    void handleGetAllPaymentByPaymentTypeResponse(List<PaymentDTO> paymentDTOS,
                                                  @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllFromAccountPaymentsByPaymentDateRange(UUID fromAccountId,
                                                                                                LocalDateTime fromPaymentDate,
                                                                                                LocalDateTime toPaymentDate);

    void handleGetAllPaymentFromAccountByPaymentDateRangeResponse(List<PaymentDTO> paymentDTOS,
                                                                  @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllToAccountPaymentsByPaymentDateRange(UUID toAccountId,
                                                                                              LocalDateTime fromPaymentDate,
                                                                                              LocalDateTime toPaymentDate);

    void handleGetAllPaymentToAccountByFromAccountResponse(List<PaymentDTO> paymentDTOS,
                                                           @Header(KafkaHeaders.CORRELATION_ID) String correlationId);
}
