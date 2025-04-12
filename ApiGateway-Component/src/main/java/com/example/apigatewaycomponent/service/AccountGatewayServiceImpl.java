package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.AccountDTO;
import com.example.apigatewaycomponent.dto.ErrorDTO;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class AccountGatewayServiceImpl implements AccountGatewayService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountGatewayServiceImpl.class);
    private static final String CREATED_EXCEPTED_FUTURE_LOG = "Creating expected future result with correlation id: {}";
    private static final String ALLOCATED_TOPIC_LOG = "Topic was created and allocated in kafka broker successfully: {}";
    private static final String REMOVED_EXPECTED_FUTURE_LOG = "Future expectation with correlation id: {} was removed from expectations";
    private static final String COMPLETED_EXPECTED_FUTURE_LOG = "Completing expected future response with: {}";
    private static final long REQUEST_TIMEOUT = 5;
    private final KafkaTemplate<String, UUID> uuidKafkaTemplate;
    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private final KafkaTemplate<String, Map<UUID, AccountDTO>> mapUUIDToDTOKafkaTemplate;
    private final KafkaTemplate<String, Map<UUID, String>> mapUUIDToStringKafkaTemplate;
    private final KafkaTemplate<String, Map<UUID, BigDecimal>> mapUUIDToBigDecimalKafkaTemplate;
    private final KafkaTemplate<String, Map<String, BigDecimal>> mapStringToBigDecimalKafkaTemplate;
    private final Map<String, CompletableFuture<ResponseEntity<Object>>> responseFutures = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<ResponseEntity<List<AccountDTO>>>> responseListFutures = new ConcurrentHashMap<>();

    public AccountGatewayServiceImpl(KafkaTemplate<String, UUID> uuidKafkaTemplate,
                                     KafkaTemplate<String, String> stringKafkaTemplate, KafkaTemplate<String, Map<UUID, AccountDTO>> mapUUIDToDTOKafkaTemplate, KafkaTemplate<String, Map<UUID, String>> mapUUIDToStringKafkaTemplate, KafkaTemplate<String, Map<UUID, BigDecimal>> mapUUIDToBigDecimalKafkaTemplate, KafkaTemplate<String, Map<String, BigDecimal>> mapStringToBigDecimalKafkaTemplate) {
        this.stringKafkaTemplate = stringKafkaTemplate;
        this.uuidKafkaTemplate = uuidKafkaTemplate;
        this.mapUUIDToDTOKafkaTemplate = mapUUIDToDTOKafkaTemplate;
        this.mapUUIDToStringKafkaTemplate = mapUUIDToStringKafkaTemplate;
        this.mapUUIDToBigDecimalKafkaTemplate = mapUUIDToBigDecimalKafkaTemplate;
        this.mapStringToBigDecimalKafkaTemplate = mapStringToBigDecimalKafkaTemplate;
    }


    @Override
    @KafkaListener(topics = "account-error", groupId = "api-gateway",
            containerFactory = "errorDTOKafkaListenerFactory")
    public void handleAccountErrors(ErrorDTO accountErrorDTO,
                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.error("Received error topic with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureErrorResponse = responseFutures.remove(correlationId);
        LOGGER.info("Complete CompletableFuture exceptionally with message: {} ", accountErrorDTO);
        futureErrorResponse.completeExceptionally(new ResponseStatusException(HttpStatus.valueOf(
                accountErrorDTO.getStatus()), accountErrorDTO.getMessage()));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> createAccount(UUID userId, AccountDTO accountDTO) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: create-account-by-user-id with correlation id: {} ", correlationId);
        Map<UUID, AccountDTO> createAccountRequestMap = Map.of(userId, accountDTO);
        ProducerRecord<String, Map<UUID, AccountDTO>> topic = new ProducerRecord<>("create-account-by-user-id",
                createAccountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        mapUUIDToDTOKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponseOrTimeout(futureResponse);
    }

    @Override
    @KafkaListener(topics = "create-card-by-account-id-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleAccountCreationResponse(AccountDTO accountDTO,
                                              @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: create-card-by-account-id with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getAccountByAccountName(String accountName) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: get-account-by-account-name with correlation id: {} ", correlationId);
        ProducerRecord<String, String> topic = new ProducerRecord<>("get-account-by-account-name", accountName);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        stringKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponseOrTimeout(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-account-by-account-name-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleGetAccountByNameResponse(AccountDTO accountDTO,
                                               @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: get-account-by-account-name with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getAccountById(UUID accountId) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: get-account-by-account-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("get-account-by-account-id", accountId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponseOrTimeout(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-account-by-account-id-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleGetAccountByIdResponse(AccountDTO accountDTO,
                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: get-account-by-account-id with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllUserAccountsByUserId(UUID userId) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: get-all-accounts-by-user-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("get-all-accounts-by-user-id", userId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponsesOrTimeout(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-account-by-account-id-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllAccountsByIdResponse(List<AccountDTO> accountDTOS,
                                                 @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: get-account-by-account-id with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = responseListFutures.remove(correlationId);
        LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, accountDTOS);
        futureResponse.complete(ResponseEntity.ok(accountDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountsByHolderFullName(String accountHolderFullName) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: get-all-accounts-by-holder-full-name with correlation id: {} ", correlationId);
        ProducerRecord<String, String> topic = new ProducerRecord<>(
                "get-all-accounts-by-holder-full-name", accountHolderFullName);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        stringKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponsesOrTimeout(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-all-accounts-by-holder-full-name-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllAccountsByHolderFullNameResponse(List<AccountDTO> accountDTOS,
                                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: get-all-accounts-by-holder-full-name with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = responseListFutures.remove(correlationId);
        LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, accountDTOS);
        futureResponse.complete(ResponseEntity.ok(accountDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getBalanceByAccountId(UUID accountId) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: get-balance-by-account-id with correlation id: {}", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("get-balance-by-account-id", accountId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponseOrTimeout(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-balance-by-account-id-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleGetAccountBalanceByIdResponse(AccountDTO accountDTO,
                                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: get-balance-by-account-id with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountsByStatus(UUID userId, String accountStatus) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: get-all-accounts-by-holder-full-name with correlation id: {}", correlationId);
        Map<UUID, String> accountRequestMap = Map.of(userId, accountStatus);
        ProducerRecord<String, Map<UUID, String>> topic = new ProducerRecord<>("get-all-accounts-by-status", accountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        mapUUIDToStringKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponsesOrTimeout(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-all-accounts-by-status-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllAccountsByStatusResponse(List<AccountDTO> accountDTOS,
                                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: get-all-accounts-by-holder-full-name with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = responseListFutures.remove(correlationId);
        LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, accountDTOS);
        futureResponse.complete(ResponseEntity.ok(accountDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> refillAccount(UUID accountId, BigDecimal amount) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: refill-account-by-account-id with correlation id: {} ", correlationId);
        Map<UUID, BigDecimal> accountRequestMap = Map.of(accountId, amount);
        ProducerRecord<String, Map<UUID, BigDecimal>> topic = new ProducerRecord<>(
                "refill-account-by-account-id", accountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        mapUUIDToBigDecimalKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponseOrTimeout(futureResponse);
    }

    @Override
    @KafkaListener(topics = "refill-account-by-account-id-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleRefillAccountByIdResponse(AccountDTO accountDTO,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: refill-account-by-account-id with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateAccountById(UUID accountId, AccountDTO accountDTO) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: update-account-by-account-id with correlation id: {} ", correlationId);
        Map<UUID, AccountDTO> createAccountRequestMap = Map.of(accountId, accountDTO);
        ProducerRecord<String, Map<UUID, AccountDTO>> topic = new ProducerRecord<>("update-account-by-account-id",
                createAccountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        mapUUIDToDTOKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponseOrTimeout(futureResponse);
    }

    @Override
    @KafkaListener(topics = "update-account-by-account-id-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleUpdateAccountByIdResponse(AccountDTO accountDTO,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: update-account-by-account-id with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateAccountStatusById(UUID accountId, String status) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: update-account-status-by-account-id with correlation id: {} ", correlationId);
        Map<UUID, String> createAccountRequestMap = Map.of(accountId, status);
        ProducerRecord<String, Map<UUID, String>> topic = new ProducerRecord<>("update-account-status-by-account-id",
                createAccountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        mapUUIDToStringKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponseOrTimeout(futureResponse);
    }

    @Override
    @KafkaListener(topics = "update-account-status-by-account-id-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleUpdateAccountStatusByIdResponse(AccountDTO accountDTO,
                                                      @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: update-account-status-by-account-id with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateAccountBalanceById(UUID accountId, BigDecimal newBalance) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: update-account-balance-by-account-id with correlation id: {} ", correlationId);
        Map<UUID, BigDecimal> createAccountRequestMap = Map.of(accountId, newBalance);
        ProducerRecord<String, Map<UUID, BigDecimal>> topic = new ProducerRecord<>("update-account-balance-by-account-id",
                createAccountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        mapUUIDToBigDecimalKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponseOrTimeout(futureResponse);
    }

    @Override
    @KafkaListener(topics = "update-account-balance-by-account-id-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleUpdateAccountBalanceByIdResponse(AccountDTO accountDTO,
                                                       @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: update-account-balance-by-account-id with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateAccountBalanceByAccountName(String accountName,
                                                                                       BigDecimal newBalance) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: update-account-balance-by-account-name with correlation id: {} ", correlationId);
        Map<String, BigDecimal> createAccountRequestMap = Map.of(accountName, newBalance);
        ProducerRecord<String, Map<String, BigDecimal>> topic = new ProducerRecord<>("update-account-balance-by-account-name",
                createAccountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        mapStringToBigDecimalKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponseOrTimeout(futureResponse);
    }

    @Override
    @KafkaListener(topics = "update-account-balance-by-account-name-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleUpdateAccountBalanceByNameResponse(AccountDTO accountDTO,
                                                         @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: update-account-balance-by-account-name with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteAccountByAccountId(UUID accountId) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: delete-account-by-account-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("delete-account-by-account-id", accountId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponseOrTimeout(futureResponse);
    }

    @Override
    @KafkaListener(topics = "delete-account-by-account-id-response", groupId = "api-gateway",
            containerFactory = "stringKafkaListenerFactory")
    public void handleDeleteAccountByIdResponse(AccountDTO accountDTO,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: delete-account-by-account-id with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteAccountByAccountName(String accountName) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: delete-account-by-account-name with correlation id: {}", correlationId);
        ProducerRecord<String, String> topic = new ProducerRecord<>("delete-account-by-account-name", accountName);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        stringKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponseOrTimeout(futureResponse);
    }

    @Override
    @KafkaListener(topics = "delete-account-by-account-name-response", groupId = "api-gateway",
            containerFactory = "stringKafkaListenerFactory")
    public void handleDeleteAccountByNameResponse(AccountDTO accountDTO,
                                                  @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: delete-account-by-account-name with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteAllUserAccountsByUserId(UUID userId) {
        String correlationId = getCorrelationId();
        LOGGER.debug(CREATED_EXCEPTED_FUTURE_LOG, correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        LOGGER.info("Trying to create topic: delete-account-by-account-id with correlation id: {}", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("delete-account-by-account-id", userId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, topic.value());
        return awaitResponseOrTimeout(futureResponse);
    }

    @Override
    @KafkaListener(topics = "delete-account-by-account-id-response", groupId = "api-gateway",
            containerFactory = "stringKafkaListenerFactory")
    public void handleDeleteAccountByUserIdResponse(AccountDTO accountDTO,
                                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Response from topic: delete-account-by-account-id with correlation id: {}", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        LOGGER.debug(REMOVED_EXPECTED_FUTURE_LOG, correlationId);
        LOGGER.info(COMPLETED_EXPECTED_FUTURE_LOG, accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    private CompletableFuture<ResponseEntity<List<Object>>> awaitResponsesOrTimeout(CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse) {
        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null) {
                        LOGGER.info("Request successfully collapsed and received to the Controller");
                        return ResponseEntity.ok((List<Object>) response);
                    } else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT,
                                "Request timed out, service unreachable, please try again later");
                    }
                });
    }

    private CompletableFuture<ResponseEntity<Object>> awaitResponseOrTimeout(CompletableFuture<ResponseEntity<Object>> futureResponse) {
        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null && !futureResponse.isDone()) {
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
