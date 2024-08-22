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
    private static final Logger logger = LoggerFactory.getLogger(AccountGatewayServiceImpl.class);
    private static final long REQUEST_TIMEOUT = 5;
    private final KafkaTemplate<String, Object> accountKafkaTemplate;
    private final Map<String, CompletableFuture<ResponseEntity<Object>>> responseFutures = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<ResponseEntity<List<AccountDTO>>>> responseListFutures = new ConcurrentHashMap<>();

    public AccountGatewayServiceImpl(KafkaTemplate<String, Object> accountKafkaTemplate) {
        this.accountKafkaTemplate = accountKafkaTemplate;
    }


    @Override
    @KafkaListener(topics = "account-error", groupId = "api-gateway",
            containerFactory = "errorDTOKafkaListenerFactory")
    public void handleAccountErrors(ErrorDTO accountErrorDTO,
                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.error("Received error topic with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureErrorResponse = responseFutures.remove(correlationId);
        logger.info("Complete CompletableFuture exceptionally with message: {} ", accountErrorDTO.toString());
        futureErrorResponse.completeExceptionally(new ResponseStatusException(HttpStatus.valueOf(
                accountErrorDTO.getStatus()), accountErrorDTO.getMessage()));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> createAccount(String userId, AccountDTO accountDTO) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: create-account-by-user-id with correlation id: {} ", correlationId);
        Map<String, Object> createAccountRequestMap = Map.of(userId, accountDTO);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("create-account-by-user-id",
                createAccountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "create-card-by-account-id-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleAccountCreationResponse(AccountDTO accountDTO,
                                              @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: create-card-by-account-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getAccountByAccountName(String accountName) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-account-by-account-name with correlation id: {} ", correlationId);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-account-by-account-name", accountName);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-account-by-account-name-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleGetAccountByNameResponse(AccountDTO accountDTO,
                                               @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-account-by-account-name with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getAccountById(String accountId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-account-by-account-id with correlation id: {} ", correlationId);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-account-by-account-id", accountId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-account-by-account-id-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleGetAccountByIdResponse(AccountDTO accountDTO,
                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-account-by-account-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllUserAccountsByUserId(String userId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-all-accounts-by-user-id with correlation id: {} ", correlationId);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-all-accounts-by-user-id", userId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntitysCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-account-by-account-id-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllAccountsByIdResponse(List<AccountDTO> accountDTOS,
                                                 @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-account-by-account-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = responseListFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", accountDTOS);
        futureResponse.complete(ResponseEntity.ok(accountDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountsByHolderFullName(String accountHolderFullName) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-all-accounts-by-holder-full-name with correlation id: {} ", correlationId);
        ProducerRecord<String, Object> topic = new ProducerRecord<>(
                "get-all-accounts-by-holder-full-name", accountHolderFullName);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntitysCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-all-accounts-by-holder-full-name-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllAccountsByHolderFullNameResponse(List<AccountDTO> accountDTOS,
                                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-all-accounts-by-holder-full-name with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = responseListFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", accountDTOS);
        futureResponse.complete(ResponseEntity.ok(accountDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getBalanceByAccountId(String accountId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-balance-by-account-id with correlation id: {} ", correlationId);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-balance-by-account-id", accountId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-balance-by-account-id-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleGetAccountBalanceByIdResponse(AccountDTO accountDTO,
                                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-balance-by-account-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountsByStatus(String userId, String accountStatus) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-all-accounts-by-holder-full-name with correlation id: {} ", correlationId);
        Map<String, Object> accountRequestMap = Map.of(userId, accountStatus);
        ProducerRecord<String, Object> topic = new ProducerRecord<>(
                "get-all-accounts-by-holder-full-name", accountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntitysCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-all-accounts-by-holder-full-name-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllAccountsByStatusResponse(List<AccountDTO> accountDTOS,
                                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-all-accounts-by-holder-full-name with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = responseListFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", accountDTOS);
        futureResponse.complete(ResponseEntity.ok(accountDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> refillAccount(String accountId, BigDecimal amount) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: refill-account-by-account-id with correlation id: {} ", correlationId);
        Map<String, Object> accountRequestMap = Map.of(accountId, amount);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("refill-account-by-account-id", accountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "refill-account-by-account-id-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleRefillAccountByIdResponse(AccountDTO accountDTO,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: refill-account-by-account-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateAccountById(String accountId, AccountDTO accountDTO) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: update-account-by-account-id with correlation id: {} ", correlationId);
        Map<String, Object> createAccountRequestMap = Map.of(accountId, accountDTO);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("update-account-by-account-id",
                createAccountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "update-account-by-account-id-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleUpdateAccountByIdResponse(AccountDTO accountDTO,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: update-account-by-account-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateAccountStatusById(String accountId, String status) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: update-account-status-by-account-id with correlation id: {} ", correlationId);
        Map<String, Object> createAccountRequestMap = Map.of(accountId, status);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("update-account-status-by-account-id",
                createAccountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "update-account-status-by-account-id-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleUpdateAccountStatusByIdResponse(AccountDTO accountDTO,
                                                      @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: update-account-status-by-account-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateAccountBalanceById(String accountId, BigDecimal newBalance) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: update-account-balance-by-account-id with correlation id: {} ", correlationId);
        Map<String, Object> createAccountRequestMap = Map.of(accountId, newBalance);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("update-account-balance-by-account-id",
                createAccountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "update-account-balance-by-account-id-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleUpdateAccountBalanceByIdResponse(AccountDTO accountDTO,
                                                       @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: update-account-balance-by-account-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateAccountBalanceByAccountName(String accountName,
                                                                                       BigDecimal newBalance) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: update-account-balance-by-account-name with correlation id: {} ", correlationId);
        Map<String, Object> createAccountRequestMap = Map.of(accountName, newBalance);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("update-account-balance-by-account-name",
                createAccountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "update-account-balance-by-account-name-response", groupId = "api-gateway",
            containerFactory = "accountDTOKafkaListenerFactory")
    public void handleUpdateAccountBalanceByNameResponse(AccountDTO accountDTO,
                                                         @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: update-account-balance-by-account-name with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteAccountByAccountId(String accountId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: delete-account-by-account-id with correlation id: {} ", correlationId);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("delete-account-by-account-id", accountId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "delete-account-by-account-id-response", groupId = "api-gateway",
            containerFactory = "stringKafkaListenerFactory")
    public void handleDeleteAccountByIdResponse(AccountDTO accountDTO,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: delete-account-by-account-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteAccountByAccountName(String accountName) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: delete-account-by-account-name with correlation id: {} ", correlationId);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("delete-account-by-account-name", accountName);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "delete-account-by-account-name-response", groupId = "api-gateway",
            containerFactory = "stringKafkaListenerFactory")
    public void handleDeleteAccountByNameResponse(AccountDTO accountDTO,
                                                  @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: delete-account-by-account-name with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteAllUserAccountsByUserId(String userId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: delete-account-by-account-id with correlation id: {} ", correlationId);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("delete-account-by-account-id", userId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic);
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "delete-account-by-account-id-response", groupId = "api-gateway",
            containerFactory = "stringKafkaListenerFactory")
    public void handleDeleteAccountByUserIdResponse(AccountDTO accountDTO,
                                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: delete-account-by-account-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", accountDTO);
        futureResponse.complete(ResponseEntity.ok(accountDTO));
    }

    private CompletableFuture<ResponseEntity<List<Object>>> getResponseEntitysCompletableFuture(
            CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse) {
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
