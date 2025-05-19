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
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentGatewayServiceImpl.class);
    private static final String CREATED_EXCEPTED_FUTURE_LOG = "Creating expected future result with correlation id: {}";
    private static final String ALLOCATED_TOPIC_LOG = "Topic was created and allocated in kafka broker successfully: {}";
    private static final String REMOVED_EXPECTED_FUTURE_LOG = "Future expectation with correlation id: {} was removed from expectations";
    private static final String COMPLETED_EXPECTED_FUTURE_LOG = "Completing expected future response with: {}";
    private static final long REQUEST_TIMEOUT = 5;
    private final KafkaTemplate<String, PaymentDTO> paymentDTOKafkaTemplate;
    private final KafkaTemplate<String, UUID> uuidKafkaTemplate;
    private final KafkaTemplate<String, List<Object>> listObjectKafkaTemplate;
    private final KafkaTemplate<String, Map<UUID, String>> mapUUIDToStringKafkaTemplate;
    private final Map<String, CompletableFuture<ResponseEntity<Object>>> responseFutures = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<ResponseEntity<List<PaymentDTO>>>> responseListFutures = new ConcurrentHashMap<>();

    public PaymentGatewayServiceImpl(KafkaTemplate<String, PaymentDTO> paymentDTOKafkaTemplate, KafkaTemplate<String, UUID> uuidKafkaTemplate, KafkaTemplate<String, List<Object>> listObjectKafkaTemplate, KafkaTemplate<String, Map<UUID, String>> mapUUIDToStringKafkaTemplate) {
        this.paymentDTOKafkaTemplate = paymentDTOKafkaTemplate;
        this.uuidKafkaTemplate = uuidKafkaTemplate;
        this.listObjectKafkaTemplate = listObjectKafkaTemplate;
        this.mapUUIDToStringKafkaTemplate = mapUUIDToStringKafkaTemplate;
    }

    @Override
    @KafkaListener(topics = "payment-error", groupId = "api-gateway",
            containerFactory = "errorDTOKafkaListenerFactory")
    public void handlePaymentErrors(ErrorDTO paymentErrorDTO,
                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.error("Received error topic with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureErrorResponse = responseFutures.get(correlationId);
        LOGGER.info("Complete CompletableFuture exceptionally with message: {} ", paymentErrorDTO);
        futureErrorResponse.completeExceptionally(new ResponseStatusException(HttpStatus.valueOf(
                paymentErrorDTO.status()), paymentErrorDTO.message()));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> createPaymentByAccounts(PaymentDTO paymentDTO) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: create-payment-by-accounts with correlation id: {} ", correlationId);
        ProducerRecord<String, PaymentDTO> topic = new ProducerRecord<>("create-payment-by-accounts", paymentDTO);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        paymentDTOKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponseOrTimeout(futureResponse, correlationId);
    }

    @Override
    @KafkaListener(topics = "create-payment-by-accounts-response", groupId = "api-gateway",
            containerFactory = "paymentDTOKafkaListenerFactory")
    public void handlePaymentCreationByAccountsResponse(PaymentDTO paymentDTO,
                                                        @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: create-payment-by-accounts with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.get(correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, paymentDTO);
        futureResponse.complete(ResponseEntity.ok(paymentDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> createPaymentByCards(String fromCardNumber,
                                                                          String toCardNumber,
                                                                          BigDecimal amount) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: create-payment-by-cards with correlation id: {} ", correlationId);
        List<Object> createPaymentByCardsRequestList = List.of(fromCardNumber, toCardNumber, amount);
        ProducerRecord<String, List<Object>> topic = new ProducerRecord<>("create-payment-by-cards",
                createPaymentByCardsRequestList);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        listObjectKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());

        return awaitResponseOrTimeout(futureResponse, correlationId);
    }

    @Override
    @KafkaListener(topics = "create-payment-by-cards-response", groupId = "api-gateway",
            containerFactory = "paymentDTOKafkaListenerFactory")
    public void handlePaymentCreationByCardsResponse(PaymentDTO paymentDTO,
                                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: create-payment-by-cards with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.get(correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, paymentDTO);
        futureResponse.complete(ResponseEntity.ok(paymentDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getPaymentById(UUID paymentId) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: get-payment-by-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("get-payment-by-id", paymentId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());

        return awaitResponseOrTimeout(futureResponse, correlationId);
    }

    @Override
    @KafkaListener(topics = "get-payment-by-id-response", groupId = "api-gateway",
            containerFactory = "paymentDTOKafkaListenerFactory")
    public void handleGetPaymentByIdResponse(PaymentDTO paymentDTO,
                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: get-payment-by-id with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.get(correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, paymentDTO);
        futureResponse.complete(ResponseEntity.ok(paymentDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountPaymentsByFromAccount(UUID fromAccountId) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: get-all-payments-by-from-account-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("get-all-payments-by-from-account-id", fromAccountId);
        uuidKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponsesOrTimeout(futureResponse, correlationId);
    }

    @Override
    @KafkaListener(topics = "get-all-payment-by-from-account-id-response", groupId = "api-gateway",
            containerFactory = "listPaymentDTOKafkaListenerFactory")
    public void handleGetAllPaymentByFromAccountResponse(List<PaymentDTO> paymentDTOS,
                                                         @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: get-all-payment-by-from-account-id with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.get(correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, paymentDTOS);
        futureResponse.complete(ResponseEntity.ok(paymentDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getPaymentsByStatus(UUID fromAccountId, String status) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: get-payments-by-status with correlation id: {} ", correlationId);
        Map<UUID, String> getPaymentByStatusRequestList = Map.of(fromAccountId, status);
        ProducerRecord<String, Map<UUID, String>> topic = new ProducerRecord<>("get-payments-by-status", getPaymentByStatusRequestList);
        mapUUIDToStringKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponsesOrTimeout(futureResponse, correlationId);
    }

    @Override
    @KafkaListener(topics = "get-payments-by-status-response", groupId = "api-gateway",
            containerFactory = "listPaymentDTOKafkaListenerFactory")
    public void handleGetAllPaymentByStatusResponse(List<PaymentDTO> paymentDTOS,
                                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: get-payments-by-status with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.get(correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, paymentDTOS);
        futureResponse.complete(ResponseEntity.ok(paymentDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountPaymentsByToAccount(UUID toAccountId) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: get-all-payments-by-to-account with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("get-all-payments-by-to-account", toAccountId);
        uuidKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponsesOrTimeout(futureResponse, correlationId);
    }

    @Override
    @KafkaListener(topics = "get-all-payments-by-to-account-response", groupId = "api-gateway",
            containerFactory = "listPaymentDTOKafkaListenerFactory")
    public void handleGetAllPaymentByToAccountResponse(List<PaymentDTO> paymentDTOS,
                                                       @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: get-all-payments-by-to-account with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.get(correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, paymentDTOS);
        futureResponse.complete(ResponseEntity.ok(paymentDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountPaymentsByPaymentType(UUID fromAccountId,
                                                                                              String paymentType) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: get-all-payments-by-payment-type with correlation id: {} ", correlationId);
        Map<UUID, String> getPaymentByTypeRequestList = Map.of(fromAccountId, paymentType);
        ProducerRecord<String, Map<UUID, String>> topic = new ProducerRecord<>(
                "get-all-payments-by-payment-type", getPaymentByTypeRequestList);
        mapUUIDToStringKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponsesOrTimeout(futureResponse, correlationId);
    }

    @Override
    @KafkaListener(topics = "get-all-payments-by-payment-type-response", groupId = "api-gateway",
            containerFactory = "listPaymentDTOKafkaListenerFactory")
    public void handleGetAllPaymentByPaymentTypeResponse(
            List<PaymentDTO> paymentDTOS, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: get-all-payments-by-payment-type with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.get(correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, paymentDTOS);
        futureResponse.complete(ResponseEntity.ok(paymentDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllFromAccountPaymentsByPaymentDateRange(
            UUID fromAccountId, LocalDateTime fromPaymentDate, LocalDateTime toPaymentDate) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: get-all-from-account-payments-by-date-range with correlation id: {} ", correlationId);
        List<Object> getPaymentByDateRangeRequestList = List.of(fromAccountId, fromPaymentDate, toPaymentDate);
        ProducerRecord<String, List<Object>> topic = new ProducerRecord<>(
                "get-all-from-account-payments-by-date-range", getPaymentByDateRangeRequestList);
        listObjectKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponsesOrTimeout(futureResponse, correlationId);
    }

    @Override
    @KafkaListener(topics = "get-all-from-account-payments-by-date-range-response", groupId = "api-gateway",
            containerFactory = "listPaymentDTOKafkaListenerFactory")
    public void handleGetAllPaymentFromAccountByPaymentDateRangeResponse(List<PaymentDTO> paymentDTOS,
                                                                         @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: get-all-from-account-payments-by-date-range with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.get(correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, paymentDTOS);
        futureResponse.complete(ResponseEntity.ok(paymentDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllToAccountPaymentsByPaymentDateRange(
            UUID toAccountId, LocalDateTime fromPaymentDate, LocalDateTime toPaymentDate) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: get-all-to-account-payments-by-date-range with correlation id: {} ", correlationId);
        List<Object> getPaymentByDateRangeRequestList = List.of(toAccountId, fromPaymentDate, toPaymentDate);
        ProducerRecord<String, List<Object>> topic = new ProducerRecord<>(
                "get-all-to-account-payments-by-date-range", getPaymentByDateRangeRequestList);
        listObjectKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponsesOrTimeout(futureResponse, correlationId);
    }


    @Override
    @KafkaListener(topics = "get-all-to-account-payments-by-date-range-response", groupId = "api-gateway",
            containerFactory = "listPaymentDTOKafkaListenerFactory")
    public void handleGetAllPaymentToAccountByFromAccountResponse(List<PaymentDTO> paymentDTOS,
                                                                  @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: get-all-to-account-payments-by-date-range with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse = responseListFutures.get(correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, paymentDTOS);
        futureResponse.complete(ResponseEntity.ok(paymentDTOS));
    }

    private CompletableFuture<ResponseEntity<List<Object>>> awaitResponsesOrTimeout(
            CompletableFuture<ResponseEntity<List<PaymentDTO>>> futureResponse, String correlationId) {
        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .whenComplete((response, throwable) -> {
                    LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
                    responseFutures.remove(correlationId);
                })
                .thenApply(response -> {
                    if (response != null && futureResponse.isDone()) {
                        LOGGER.info("Request successfully collapsed and received to the Controller");
                        return ResponseEntity.ok(new ArrayList<>(Objects.requireNonNull(response.getBody())));
                    } else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT,
                                "Request timed out, service unreachable, please try again later");
                    }
                });
    }

    private CompletableFuture<ResponseEntity<Object>> awaitResponseOrTimeout(
            CompletableFuture<ResponseEntity<Object>> futureResponse, String correlationId) {
        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .whenComplete((response, throwable) -> {
                    LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
                    responseFutures.remove(correlationId);
                })
                .thenApply(response -> {
                    if (response != null && futureResponse.isDone()) {
                        LOGGER.info("Request successfully collapsed and received to the Controller");
                        return ResponseEntity.ok(response.getBody());
                    } else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT,
                                "Request timed out, service unreachable, please try again later");
                    }
                });
    }

    private static String getCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
