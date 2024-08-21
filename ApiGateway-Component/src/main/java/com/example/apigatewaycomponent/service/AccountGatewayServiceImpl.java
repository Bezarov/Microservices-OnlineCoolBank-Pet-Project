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
import java.util.HashMap;
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
        if (futureErrorResponse != null) {
            logger.info("Complete CompletableFuture exceptionally with message: {} ", accountErrorDTO.toString());
            futureErrorResponse.completeExceptionally(new ResponseStatusException(HttpStatus.valueOf(
                    accountErrorDTO.getStatus()), accountErrorDTO.getMessage()));
        } else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> createAccount(String userId, AccountDTO accountDTO) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        Map<String, Object> createAccountRequestMap = new HashMap<>();
        createAccountRequestMap.put(userId, accountDTO);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("create-account-by-user-id",
                createAccountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleAccountCreationResponse(AccountDTO accountDTO,
                                              @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(accountDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getAccountByAccountName(String accountName) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-account-by-account-name", accountName);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAccountByNameResponse(AccountDTO accountDTO,
                                               @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(accountDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getAccountById(String accountId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-account-by-account-id", accountId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAccountByIdResponse(AccountDTO accountDTO,
                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(accountDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllUserAccountsByUserId(String userId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-all-accounts-by-user-id", userId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

        return getResponseEntitysCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAllAccountsByIdResponse(List<AccountDTO> accountDTOS,
                                                 @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = responseListFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(accountDTOS));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountsByHolderFullName(String accountHolderFullName) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>(
                "get-all-accounts-by-holder-full-name", accountHolderFullName);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

        return getResponseEntitysCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAllAccountsByHolderFullNameResponse(List<AccountDTO> accountDTOS,
                                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = responseListFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(accountDTOS));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getBalanceByAccountId(String accountId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-balance-by-account-id", accountId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAccountBalanceByIdResponse(AccountDTO accountDTO,
                                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(accountDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountsByStatus(String userId, String accountStatus) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        Map<String, Object> accountRequestMap = new HashMap<>();
        accountRequestMap.put(userId, accountStatus);
        ProducerRecord<String, Object> topic = new ProducerRecord<>(
                "get-all-accounts-by-holder-full-name", accountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

        return getResponseEntitysCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAllAccountsByStatusResponse(List<AccountDTO> accountDTOS,
                                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse = responseListFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(accountDTOS));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> refillAccount(String accountId, BigDecimal amount) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        Map<String, Object> accountRequestMap = new HashMap<>();
        accountRequestMap.put(accountId, amount);

        ProducerRecord<String, Object> topic = new ProducerRecord<>(
                "refill-account-by-account-id", accountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleRefillAccountByIdResponse(AccountDTO accountDTO,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(accountDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateAccountById(String accountId, AccountDTO accountDTO) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        Map<String, Object> createAccountRequestMap = new HashMap<>();
        createAccountRequestMap.put(accountId, accountDTO);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("update-account-by-account-id",
                createAccountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleUpdateAccountByIdResponse(AccountDTO accountDTO,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(accountDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateAccountStatusById(String accountId, String status) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        Map<String, Object> createAccountRequestMap = new HashMap<>();
        createAccountRequestMap.put(accountId, status);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("update-account-status-by-account-id",
                createAccountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleUpdateAccountStatusByIdResponse(AccountDTO accountDTO,
                                                      @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(accountDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateAccountBalanceById(String accountId, BigDecimal newBalance) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        Map<String, Object> createAccountRequestMap = new HashMap<>();
        createAccountRequestMap.put(accountId, newBalance);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("update-account-balance-by-account-id",
                createAccountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleUpdateAccountBalanceByIdResponse(AccountDTO accountDTO,
                                                       @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(accountDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateAccountBalanceByAccountName(String accountName,
                                                                                       BigDecimal newBalance) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        Map<String, Object> createAccountRequestMap = new HashMap<>();
        createAccountRequestMap.put(accountName, newBalance);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("update-account-balance-by-account-name",
                createAccountRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleUpdateAccountBalanceByNameResponse(AccountDTO accountDTO,
                                                         @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(accountDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteAccountByAccountId(String accountId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("delete-account-by-account-id", accountId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleDeleteAccountByIdResponse(AccountDTO accountDTO,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(accountDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteAccountByAccountName(String accountName) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("delete-account-by-account-name", accountName);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleDeleteAccountByNameResponse(AccountDTO accountDTO,
                                                  @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(accountDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteAllUserAccountsByUserId(String userId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("delete-account-by-account-id", userId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleDeleteAccountByUserIdResponse(AccountDTO accountDTO,
                                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(accountDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    private CompletableFuture<ResponseEntity<List<Object>>> getResponseEntitysCompletableFuture(
            String correlationId, CompletableFuture<ResponseEntity<List<AccountDTO>>> futureResponse,
            ProducerRecord<String, Object> topic) {
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        accountKafkaTemplate.send(topic);

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
        accountKafkaTemplate.send(topic);

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
