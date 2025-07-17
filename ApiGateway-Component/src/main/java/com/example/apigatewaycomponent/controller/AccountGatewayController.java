package com.example.apigatewaycomponent.controller;

import com.example.apigatewaycomponent.dto.AccountDTO;
import com.example.apigatewaycomponent.dto.AccountRefillRequestDTO;
import com.example.apigatewaycomponent.dto.AccountUpdateRequestDTO;
import com.example.apigatewaycomponent.service.AccountGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/account")
public class AccountGatewayController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountGatewayController.class);
    private static final String RESPONSE_LOG = "Request was successfully processed and response was sent: {}";

    private final AccountGatewayService accountGatewayService;

    public AccountGatewayController(AccountGatewayService accountGatewayService) {
        this.accountGatewayService = accountGatewayService;
    }

    @PostMapping("/by-user-id/{userId}")
    public CompletableFuture<ResponseEntity<Object>> createAccount(@PathVariable UUID userId,
                                                                   @RequestBody AccountDTO accountDTO) {
        LOGGER.debug("Received POST request to create Account for User with ID: {}, Account: {}",
                userId, accountDTO);
        return accountGatewayService.createAccount(userId, accountDTO)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/by-account-name/{accountName}")
    public CompletableFuture<ResponseEntity<Object>> getAccountByAccountName(@PathVariable String accountName) {
        LOGGER.debug("Received GET request to get Account by ACCOUNT NAME: {}", accountName);
        return accountGatewayService.getAccountByAccountName(accountName)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/by-account-id/{accountId}")
    public CompletableFuture<ResponseEntity<Object>> getAccountById(@PathVariable UUID accountId) {
        LOGGER.debug("Received GET request to get Account by ID: {}", accountId);
        return accountGatewayService.getAccountById(accountId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/all/by-holder-name/{accountHolderFullName}")
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountsByHolderFullName(@PathVariable
                                                                                          String accountHolderFullName) {
        LOGGER.debug("Received GET request to get All User Accounts by Holder Full name: {}",
                accountHolderFullName);
        return accountGatewayService.getAllAccountsByHolderFullName(accountHolderFullName)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/by-account-id/{accountId}/balance")
    public CompletableFuture<ResponseEntity<Object>> getBalanceByAccountId(@PathVariable UUID accountId) {
        LOGGER.debug("Received GET request to get Account balance by ACCOUNT ID: {}", accountId);
        return accountGatewayService.getBalanceByAccountId(accountId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/by-user-id/{userId}")
    public CompletableFuture<ResponseEntity<List<Object>>> getAllUserAccountsByUserId(@PathVariable UUID userId) {
        LOGGER.debug("Received GET request to get All User Accounts by USER ID: {}", userId);
        return accountGatewayService.getAllUserAccountsByUserId(userId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/by-user-id/{userId}/status")
    public CompletableFuture<ResponseEntity<List<Object>>> getAccountsByStatus(
            @PathVariable UUID userId, @RequestParam(name = "status") String accountStatus) {
        LOGGER.debug("Received GET request to get All User (USER ID: {}), Accounts by ACCOUNT STATUS: {}",
                userId, accountStatus);
        return accountGatewayService.getAllAccountsByStatus(userId, accountStatus)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @PatchMapping("/by-account-id/{accountId}/refill")
    public CompletableFuture<ResponseEntity<Object>> refillAccount(@PathVariable UUID accountId,
                                                                   @RequestParam(name = "amount") BigDecimal amount) {
        LOGGER.debug("Received PATCH request to refill Account with ID: {}, in AMOUNT OF: {}",
                accountId, amount);
        return accountGatewayService.refillAccount(new AccountRefillRequestDTO(accountId, amount))
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @PutMapping("/by-account-id/{accountId}")
    public CompletableFuture<ResponseEntity<Object>> updateAccountById(@PathVariable UUID accountId,
                                                                       @RequestBody AccountDTO accountDTO) {
        LOGGER.debug("Received PUT request to update Account with ID: {}, UPDATE TO: {}",
                accountId, accountDTO);
        return accountGatewayService.updateAccountById(new AccountUpdateRequestDTO(accountId, accountDTO))
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @PatchMapping("/by-account-id/{accountId}/status")
    public CompletableFuture<ResponseEntity<Object>> updateAccountStatusById(@PathVariable UUID accountId,
                                                                             @RequestParam String status) {
        LOGGER.debug("Received PATCH request to update Account Status with ID: {}, TO: {}",
                accountId, status);
        return accountGatewayService.updateAccountStatusById(accountId, status)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @PatchMapping("/by-account-id/{accountId}/balance")
    public CompletableFuture<ResponseEntity<Object>> updateAccountBalanceById(@PathVariable UUID accountId,
                                                                              @RequestParam BigDecimal balance) {
        LOGGER.debug("Received PATCH request to update Account Balance with ID: {}, WITH New Balance: {}",
                accountId, balance);
        return accountGatewayService.updateAccountBalanceById(accountId, balance)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @PatchMapping("/by-account-name/{accountName}/balance")
    public CompletableFuture<ResponseEntity<Object>> updateAccountBalanceByAccountName(@PathVariable String accountName,
                                                                                       @RequestParam BigDecimal balance) {
        LOGGER.debug("Received PATCH request to update Account Balance with ID: {}, WITH New Balance: {}",
                accountName, balance);
        return accountGatewayService.updateAccountBalanceByAccountName(accountName, balance)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @DeleteMapping("/by-account-id/{accountId}")
    public CompletableFuture<ResponseEntity<Object>> deleteAccountByAccountId(@PathVariable UUID accountId) {
        LOGGER.debug("Received DELETE request to remove Account with ID: {} ", accountId);
        return accountGatewayService.deleteAccountByAccountId(accountId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @DeleteMapping("/by-account-name/{accountName}")
    public CompletableFuture<ResponseEntity<Object>> deleteAccountByAccountName(@PathVariable String accountName) {
        LOGGER.debug("Received DELETE request to remove Account with Name: {} ", accountName);
        return accountGatewayService.deleteAccountByAccountName(accountName)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @DeleteMapping("/by-user-id/{userId}")
    public CompletableFuture<ResponseEntity<Object>> deleteAllUserAccountsByUserId(@PathVariable UUID userId) {
        LOGGER.debug("Received DELETE request to remove All User Accounts with User ID: {} ", userId);
        return accountGatewayService.deleteAllAccountsByUserId(userId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }
}