package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.AccountDTO;
import com.example.apigatewaycomponent.dto.AccountRefillRequestDTO;
import com.example.apigatewaycomponent.dto.ErrorDTO;
import com.example.apigatewaycomponent.dto.AccountUpdateRequestDTO;
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

    void handleGetAccountBalanceByIdResponse(BigDecimal balance, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllAccountsByStatus(UUID userId, String accountStatus);

    void handleGetAllAccountsByStatusResponse(List<AccountDTO> accountDTOS, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> refillAccount(AccountRefillRequestDTO accountRefillRequestDTO);

    void handleRefillAccountByIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updateAccountById(AccountUpdateRequestDTO accountUpdateRequestDTO);

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

    CompletableFuture<ResponseEntity<Object>> deleteAllAccountsByUserId(UUID userId);

    void handleDeleteAllAccountByUserIdResponse(AccountDTO accountDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);
}
