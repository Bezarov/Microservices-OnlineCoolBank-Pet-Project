package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.ErrorDTO;
import com.example.apigatewaycomponent.dto.PaymentDTO;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public void handlePaymentErrors(ErrorDTO paymentErrorDTO,
                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.error("Received error topic with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureErrorResponse = responseFutures.remove(correlationId);
        if (futureErrorResponse != null) {
            logger.info("Complete CompletableFuture exceptionally with message: {} ", paymentErrorDTO.toString());
            futureErrorResponse.completeExceptionally(new ResponseStatusException(HttpStatus.valueOf(
                    paymentErrorDTO.getStatus()), paymentErrorDTO.getMessage()));
        } else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> createPaymentByAccounts(PaymentDTO paymentDTO) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("create-payment-by-accounts", paymentDTO);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        paymentKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handlePaymentCreationByAccountsResponse(PaymentDTO paymentDTO,
                                                        @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(paymentDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> createPaymentByCards(String fromCardNumber,
                                                                          String toCardNumber,
                                                                          BigDecimal amount) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        List<Object> createPaymentByCardsRequestMap = new ArrayList<>();
        createPaymentByCardsRequestMap.add(fromCardNumber);
        createPaymentByCardsRequestMap.add(toCardNumber);
        createPaymentByCardsRequestMap.add(amount);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("create-payment-by-cards",
                createPaymentByCardsRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        paymentKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handlePaymentCreationByCardsResponse(PaymentDTO paymentDTO,
                                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(paymentDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getPaymentById(String paymentId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-payment-by-id", paymentId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        paymentKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetPaymentByIdResponse(PaymentDTO paymentDTO,
                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(paymentDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountPaymentsByFromAccount(String fromAccountId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-all-payments-by-from-account", fromAccountId);
        return getResponseEntitysCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAllPaymentByFromAccountResponse(List<PaymentDTO> paymentDTOS,
                                                         @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(paymentDTOS));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getPaymentsByStatus(String fromAccountId, String status) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        List<Object> getPaymentByStatusRequestMap = new ArrayList<>();
        getPaymentByStatusRequestMap.add(fromAccountId);
        getPaymentByStatusRequestMap.add(status);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-payments-by-status", getPaymentByStatusRequestMap);
        return getResponseEntitysCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAllPaymentByStatusResponse(List<PaymentDTO> paymentDTOS,
                                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(paymentDTOS));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountPaymentsByToAccount(String toAccountId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-all-payments-by-to-account", toAccountId);
        return getResponseEntitysCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAllPaymentByToAccountResponse(List<PaymentDTO> paymentDTOS,
                                                       @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(paymentDTOS));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountPaymentsByPaymentType(String fromAccountId,
                                                                                              String paymentType) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        List<Object> getPaymentByTypeRequestMap = new ArrayList<>();
        getPaymentByTypeRequestMap.add(fromAccountId);
        getPaymentByTypeRequestMap.add(paymentType);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-all-payments-by-payment-type",
                getPaymentByTypeRequestMap);
        return getResponseEntitysCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAllPaymentByPaymentTypeResponse(
            List<PaymentDTO> paymentDTOS, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(paymentDTOS));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllFromAccountPaymentsByPaymentDateRange(
            String fromAccountId, LocalDateTime fromPaymentDate, LocalDateTime toPaymentDate) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        List<Object> getPaymentByDateRangeRequestMap = new ArrayList<>();
        getPaymentByDateRangeRequestMap.add(fromAccountId);
        getPaymentByDateRangeRequestMap.add(fromPaymentDate);
        getPaymentByDateRangeRequestMap.add(toPaymentDate);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-all-from-account-payments-by-date-range",
                getPaymentByDateRangeRequestMap);
        return getResponseEntitysCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAllPaymentFromAccountByPaymentDateRangeResponse(List<PaymentDTO> paymentDTOS,
                                                                         @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(paymentDTOS));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllToAccountPaymentsByPaymentDateRange(
            String toAccountId, LocalDateTime fromPaymentDate, LocalDateTime toPaymentDate) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        List<Object> getPaymentByDateRangeRequestMap = new ArrayList<>();
        getPaymentByDateRangeRequestMap.add(toAccountId);
        getPaymentByDateRangeRequestMap.add(fromPaymentDate);
        getPaymentByDateRangeRequestMap.add(toPaymentDate);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-all-to-account-payments-by-date-range",
                getPaymentByDateRangeRequestMap);
        return getResponseEntitysCompletableFuture(correlationId, futureResponse, topic);
    }


    @Override
    public void handleGetAllPaymentToAccountByFromAccountResponse(List<PaymentDTO> paymentDTOS,
                                                                  @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(paymentDTOS));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    private CompletableFuture<ResponseEntity<List<Object>>> getResponseEntitysCompletableFuture(
            String correlationId, CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse,
            ProducerRecord<String, Object> topic) {
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        paymentKafkaTemplate.send(topic);

        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null)
                        return ResponseEntity.ok((List<Object>) response);
                    else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
                    }
                })
                .exceptionally(error -> {
                    if (error.getCause() instanceof ResponseStatusException)
                        throw (ResponseStatusException) error.getCause();
                    else {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                    }
                });
    }

    private CompletableFuture<ResponseEntity<Object>> getResponseEntityCompletableFuture(
            String correlationId, CompletableFuture<ResponseEntity<Object>> futureResponse,
            ProducerRecord<String, Object> topic) {
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        paymentKafkaTemplate.send(topic);

        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null)
                        return ResponseEntity.ok(response.getBody());
                    else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
                    }
                })
                .exceptionally(error -> {
                    if (error.getCause() instanceof ResponseStatusException)
                        throw (ResponseStatusException) error.getCause();
                    else {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                    }
                });
    }
}
