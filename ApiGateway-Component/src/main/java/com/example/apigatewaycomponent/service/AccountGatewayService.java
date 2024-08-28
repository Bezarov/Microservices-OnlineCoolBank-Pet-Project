package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.AccountDTO;
import com.example.apigatewaycomponent.dto.ErrorDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AccountGatewayService {
    void handleAccountErrors(ErrorDTO accountErrorDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> createAccount(UUID userId, AccountDTO accountDTO);

    void handleAccountCreationResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getAccountByAccountName(String accountName);

    void handleGetAccountByNameResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getAccountById(UUID accountId);

    void handleGetAccountByIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllUserAccountsByUserId(UUID userId);

    void handleGetAllAccountsByIdResponse(List<AccountDTO> accountDTOS,
                                          @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllAccountsByHolderFullName(String accountHolderFullName);

    void handleGetAllAccountsByHolderFullNameResponse(List<AccountDTO> accountDTOS,
                                                      @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getBalanceByAccountId(UUID accountId);

    void handleGetAccountBalanceByIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllAccountsByStatus(UUID userId, String accountStatus);

    void handleGetAllAccountsByStatusResponse(List<AccountDTO> accountDTOS, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> refillAccount(UUID accountId, BigDecimal amount);

    void handleRefillAccountByIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updateAccountById(UUID accountId, AccountDTO accountDTO);

    void handleUpdateAccountByIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updateAccountStatusById(UUID accountId, String status);

    void handleUpdateAccountStatusByIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updateAccountBalanceById(UUID accountId, BigDecimal newBalance);

    void handleUpdateAccountBalanceByIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updateAccountBalanceByAccountName(String accountName, BigDecimal balance);

    void handleUpdateAccountBalanceByNameResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> deleteAccountByAccountId(UUID accountId);

    void handleDeleteAccountByIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> deleteAccountByAccountName(String accountName);

    void handleDeleteAccountByNameResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> deleteAllUserAccountsByUserId(UUID userId);

    void handleDeleteAccountByUserIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);
}
