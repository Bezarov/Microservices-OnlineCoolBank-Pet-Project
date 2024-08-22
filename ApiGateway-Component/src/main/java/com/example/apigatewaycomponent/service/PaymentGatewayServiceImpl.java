package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.ErrorDTO;
import com.example.apigatewaycomponent.dto.PaymentDTO;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentGatewayServiceImpl implements PaymentGatewayService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayServiceImpl.class);
    private static final long REQUEST_TIMEOUT = 5;
    private final KafkaTemplate<String, Object> paymentKafkaTemplate;
    private final Map<String, CompletableFuture<ResponseEntity<Object>>> responseFutures = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<ResponseEntity<List<PaymentDTO>>>> responseListFutures = new ConcurrentHashMap<>();

    public PaymentGatewayServiceImpl(KafkaTemplate<String, Object> paymentKafkaTemplate) {
        this.paymentKafkaTemplate = paymentKafkaTemplate;
    }

    @Override
    @KafkaListener(topics = "payment-error", groupId = "api-gateway",
            containerFactory = "errorDTOKafkaListenerFactory")
    public void handlePaymentErrors(ErrorDTO paymentErrorDTO,
                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.error("Received error topic with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureErrorResponse = responseFutures.remove(correlationId);
        logger.info("Complete CompletableFuture exceptionally with message: {} ", paymentErrorDTO.toString());
        futureErrorResponse.completeExceptionally(new ResponseStatusException(HttpStatus.valueOf(
                paymentErrorDTO.getStatus()), paymentErrorDTO.getMessage()));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> createPaymentByAccounts(PaymentDTO paymentDTO) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: create-payment-by-accounts with correlation id: {} ", correlationId);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("create-payment-by-accounts", paymentDTO);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        paymentKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);

        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "create-payment-by-accounts-response", groupId = "api-gateway",
            containerFactory = "paymentDTOKafkaListenerFactory")
    public void handlePaymentCreationByAccountsResponse(PaymentDTO paymentDTO,
                                                        @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: create-payment-by-accounts with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", paymentDTO);
        futureResponse.complete(ResponseEntity.ok(paymentDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> createPaymentByCards(String fromCardNumber,
                                                                          String toCardNumber,
                                                                          BigDecimal amount) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: create-payment-by-cards with correlation id: {} ", correlationId);
        List<Object> createPaymentByCardsRequestList = List.of(fromCardNumber, toCardNumber, amount);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("create-payment-by-cards",
                createPaymentByCardsRequestList);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        paymentKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);

        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "create-payment-by-cards-response", groupId = "api-gateway",
            containerFactory = "paymentDTOKafkaListenerFactory")
    public void handlePaymentCreationByCardsResponse(PaymentDTO paymentDTO,
                                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: create-payment-by-cards with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", paymentDTO);
        futureResponse.complete(ResponseEntity.ok(paymentDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getPaymentById(String paymentId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-payment-by-id with correlation id: {} ", correlationId);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-payment-by-id", paymentId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        paymentKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);

        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-payment-by-id-response", groupId = "api-gateway",
            containerFactory = "paymentDTOKafkaListenerFactory")
    public void handleGetPaymentByIdResponse(PaymentDTO paymentDTO,
                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: create-payment-by-cards with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", paymentDTO);
        futureResponse.complete(ResponseEntity.ok(paymentDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountPaymentsByFromAccount(String fromAccountId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-all-payments-by-from-account-id with correlation id: {} ", correlationId);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-all-payments-by-from-account-id", fromAccountId);
        paymentKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntitysCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-all-payment-by-from-account-id-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllPaymentByFromAccountResponse(List<PaymentDTO> paymentDTOS,
                                                         @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-all-payment-by-from-account-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", paymentDTOS);
        futureResponse.complete(ResponseEntity.ok(paymentDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getPaymentsByStatus(String fromAccountId, String status) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-payments-by-status with correlation id: {} ", correlationId);
        List<Object> getPaymentByStatusRequestList = List.of(fromAccountId, status);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-payments-by-status", getPaymentByStatusRequestList);
        paymentKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntitysCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-payments-by-status-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllPaymentByStatusResponse(List<PaymentDTO> paymentDTOS,
                                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-payments-by-status with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", paymentDTOS);
        futureResponse.complete(ResponseEntity.ok(paymentDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountPaymentsByToAccount(String toAccountId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-all-payments-by-to-account with correlation id: {} ", correlationId);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-all-payments-by-to-account", toAccountId);
        paymentKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntitysCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-all-payments-by-to-account-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllPaymentByToAccountResponse(List<PaymentDTO> paymentDTOS,
                                                       @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-all-payments-by-to-account with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", paymentDTOS);
        futureResponse.complete(ResponseEntity.ok(paymentDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountPaymentsByPaymentType(String fromAccountId,
                                                                                              String paymentType) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-all-payments-by-payment-type with correlation id: {} ", correlationId);
        List<Object> getPaymentByTypeRequestList = List.of(futureResponse, paymentType);
        ProducerRecord<String, Object> topic = new ProducerRecord<>(
                "get-all-payments-by-payment-type", getPaymentByTypeRequestList);
        paymentKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntitysCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-all-payments-by-payment-type-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllPaymentByPaymentTypeResponse(
            List<PaymentDTO> paymentDTOS, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-all-payments-by-payment-type with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", paymentDTOS);
        futureResponse.complete(ResponseEntity.ok(paymentDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllFromAccountPaymentsByPaymentDateRange(
            String fromAccountId, LocalDateTime fromPaymentDate, LocalDateTime toPaymentDate) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-all-from-account-payments-by-date-range with correlation id: {} ", correlationId);
        List<Object> getPaymentByDateRangeRequestList = List.of(fromAccountId, fromPaymentDate, toPaymentDate);
        ProducerRecord<String, Object> topic = new ProducerRecord<>(
                "get-all-from-account-payments-by-date-range", getPaymentByDateRangeRequestList);
        paymentKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntitysCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-all-from-account-payments-by-date-range-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllPaymentFromAccountByPaymentDateRangeResponse(List<PaymentDTO> paymentDTOS,
                                                                         @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-all-payments-by-payment-type with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", paymentDTOS);
        futureResponse.complete(ResponseEntity.ok(paymentDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllToAccountPaymentsByPaymentDateRange(
            String toAccountId, LocalDateTime fromPaymentDate, LocalDateTime toPaymentDate) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-all-to-account-payments-by-date-range with correlation id: {} ", correlationId);
        List<Object> getPaymentByDateRangeRequestList = List.of(toAccountId, fromPaymentDate, toPaymentDate);
        ProducerRecord<String, Object> topic = new ProducerRecord<>(
                "get-all-to-account-payments-by-date-range", getPaymentByDateRangeRequestList);
        paymentKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntitysCompletableFuture(futureResponse);
    }


    @Override
    @KafkaListener(topics = "get-all-to-account-payments-by-date-range-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllPaymentToAccountByFromAccountResponse(List<PaymentDTO> paymentDTOS,
                                                                  @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-all-to-account-payments-by-date-range with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", paymentDTOS);
        futureResponse.complete(ResponseEntity.ok(paymentDTOS));
    }

    private CompletableFuture<ResponseEntity<List<Object>>> getResponseEntitysCompletableFuture(
            CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse) {
        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null) {
                        logger.info("Request successfully collapsed and received to the Controller");
                        return ResponseEntity.ok((List<Object>) response);
                    } else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT,
                                "Request timed out, service unreachable, please try again later");
                    }
                });
    }

    private CompletableFuture<ResponseEntity<Object>> getResponseEntityCompletableFuture(
            CompletableFuture<ResponseEntity<Object>> futureResponse) {
        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null) {
                        logger.info("Request successfully collapsed and received to the Controller");
                        return ResponseEntity.ok(response.getBody());
                    } else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT,
                                "Request timed out, service unreachable, please try again later");
                    }
                });
    }
}