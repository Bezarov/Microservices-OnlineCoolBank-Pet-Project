package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.AccountDTO;
import com.example.apigatewaycomponent.dto.ErrorDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AccountGatewayService {
    void handleAccountErrors(ErrorDTO accountErrorDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> createAccount(String userId, AccountDTO accountDTO);

    void handleAccountCreationResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getAccountByAccountName(String accountName);

    void handleGetAccountByNameResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getAccountById(String accountId);

    void handleGetAccountByIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllUserAccountsByUserId(String userId);

    void handleGetAllAccountsByIdResponse(List<AccountDTO> accountDTOS,
                                          @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllAccountsByHolderFullName(String accountHolderFullName);

    void handleGetAllAccountsByHolderFullNameResponse(List<AccountDTO> accountDTOS,
                                                      @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getBalanceByAccountId(String accountId);

    void handleGetAccountBalanceByIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllAccountsByStatus(String userId, String accountStatus);

    void handleGetAllAccountsByStatusResponse(List<AccountDTO> accountDTOS, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> refillAccount(String accountId, BigDecimal amount);

    void handleRefillAccountByIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updateAccountById(String accountId, AccountDTO accountDTO);

    void handleUpdateAccountByIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updateAccountStatusById(String accountId, String status);

    void handleUpdateAccountStatusByIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updateAccountBalanceById(String accountId, BigDecimal newBalance);

    void handleUpdateAccountBalanceByIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updateAccountBalanceByAccountName(String accountName, BigDecimal balance);

    void handleUpdateAccountBalanceByNameResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> deleteAccountByAccountId(String accountId);

    void handleDeleteAccountByIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> deleteAccountByAccountName(String accountName);

    void handleDeleteAccountByNameResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> deleteAllUserAccountsByUserId(String userId);

    void handleDeleteAccountByUserIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);
}
