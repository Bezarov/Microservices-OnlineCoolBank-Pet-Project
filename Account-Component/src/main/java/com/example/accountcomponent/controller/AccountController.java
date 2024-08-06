package com.example.accountcomponent.controller;

import com.example.accountcomponent.dto.AccountDTO;
import com.example.accountcomponent.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<AccountDTO> createAccount(@PathVariable UUID userId,
                                                    @RequestBody AccountDTO accountDTO) {
        logger.info("Received POST request to create Account for User with ID: {}, Account: {}",
                userId, accountDTO);
        AccountDTO responseAccountDTO = accountService.createAccount(userId, accountDTO);
        logger.debug("Request was successfully processed and response was sent: {} ", responseAccountDTO);
        return ResponseEntity.ok(responseAccountDTO);
    }

    @GetMapping("/by-account-id/{accountId}")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable UUID accountId) {
        logger.info("Received GET request to get Account by ID: {}", accountId);
        AccountDTO responseAccountDTO = accountService.getAccountById(accountId);
        logger.debug("Request was successfully processed and response was sent: {}", responseAccountDTO);
        return ResponseEntity.ok(responseAccountDTO);
    }


    @GetMapping("/by-holder-name/{accountHolderFullName}")
    public ResponseEntity<List<AccountDTO>> getAllAccountsByHolderFullName(@PathVariable
                                                                           String accountHolderFullName) {
        logger.info("Received GET request to get All User Accounts by HOLDER FULL NAME: {}",
                accountHolderFullName);
        List<AccountDTO> responseAccountDTOS = accountService.getAllAccountsByHolderFullName(accountHolderFullName);
        logger.debug("Request was successfully processed and response was sent: {}", responseAccountDTOS);
        return ResponseEntity.ok(responseAccountDTOS);
    }

    @GetMapping("/by-account-id/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalanceByAccountId(@PathVariable UUID accountId) {
        logger.info("Received GET request to get Account balance by ACCOUNT ID: {}", accountId);
        BigDecimal responseBigDecimal = accountService.getBalanceByAccountId(accountId);
        logger.debug("Request was successfully processed and response was sent: {}", responseBigDecimal);
        return ResponseEntity.ok(responseBigDecimal);
    }

    @GetMapping("/by-account-name/{accountName}")
    public ResponseEntity<AccountDTO> getAccountByAccountName(@PathVariable String accountName) {
        logger.info("Received GET request to get Account by ACCOUNT NAME: {}", accountName);
        AccountDTO responseAccountDTO = accountService.getAccountByAccountName(accountName);
        logger.debug("Request was successfully processed and response was sent: {}", responseAccountDTO);
        return ResponseEntity.ok(responseAccountDTO);
    }

    @GetMapping("/by-user-id/{userId}")
    public ResponseEntity<List<AccountDTO>> getAllUserAccountsByUserId(@PathVariable UUID userId) {
        logger.info("Received GET request to get All User Accounts by USER ID: {}", userId);
        List<AccountDTO> responseAccountDTOS = accountService.getAllUserAccountsByUserId(userId);
        logger.debug("Request was successfully processed and response was sent: {}", responseAccountDTOS);
        return ResponseEntity.ok(responseAccountDTOS);
    }

    @GetMapping("/by-user-id/{userId}/status")
    public ResponseEntity<List<AccountDTO>> getAccountsByStatus(
            @PathVariable UUID userId, @RequestParam(name = "status") String accountStatus) {
        logger.info("Received GET request to get All User (USER ID: {}), Accounts by ACCOUNT STATUS: {}",
                userId, accountStatus);
        List<AccountDTO> responseAccountDTOS = accountService.getAllAccountsByStatus(userId, accountStatus);
        logger.debug("Request was successfully processed and response was sent: {}", responseAccountDTOS);
        return ResponseEntity.ok(responseAccountDTOS);
    }

    @PatchMapping("/by-account-id/{accountId}/refill")
    public ResponseEntity<AccountDTO> refillAccount(@PathVariable UUID accountId,
                                                    @RequestParam(name = "amount") BigDecimal amount) {
        logger.info("Received PATCH request to refill Account with ID: {}, in AMOUNT OF: {}",
                accountId, amount);
        AccountDTO responseAccountDTO = accountService.refillAccount(accountId, amount);
        logger.debug("Request was successfully processed and response was sent: {}", responseAccountDTO);
        return ResponseEntity.ok(responseAccountDTO);
    }

    @PutMapping("/by-account-id/{accountId}")
    public ResponseEntity<AccountDTO> updateAccountById(@PathVariable UUID accountId,
                                                        @RequestBody AccountDTO accountDTO) {
        logger.info("Received PUT request to update Account with ID: {}, UPDATE TO: {}",
                accountId, accountDTO);
        AccountDTO responseAccountDTO = accountService.updateAccountById(accountId, accountDTO);
        logger.debug("Request was successfully processed and response was sent: {}", responseAccountDTO);
        return ResponseEntity.ok(responseAccountDTO);
    }

    @PatchMapping("/by-account-id/{accountId}/status")
    public ResponseEntity<AccountDTO> updateAccountStatusById(@PathVariable UUID accountId,
                                                              @RequestParam String status) {
        logger.info("Received PATCH request to update Account Status with ID: {}, TO: {}",
                accountId, status);
        AccountDTO responseAccountDTO = accountService.updateAccountStatusById(accountId, status);
        logger.debug("Request was successfully processed and response was sent: {}", responseAccountDTO);
        return ResponseEntity.ok(responseAccountDTO);
    }

    @PatchMapping("/by-account-id/{accountId}/balance")
    public ResponseEntity<AccountDTO> updateAccountBalanceById(@PathVariable UUID accountId,
                                                               @RequestParam BigDecimal balance) {
        logger.info("Received PATCH request to update Account Balance with ID: {}, WITH New Balance: {}",
                accountId, balance);
        AccountDTO responseAccountDTO = accountService.updateAccountBalanceById(accountId, balance);
        logger.debug("Request was successfully processed and response was sent: {}", responseAccountDTO);
        return ResponseEntity.ok(responseAccountDTO);
    }

    @PatchMapping("/by-account-name/{accountName}/balance")
    public ResponseEntity<AccountDTO> updateAccountBalanceByAccountName(@PathVariable String accountName,
                                                                        @RequestParam BigDecimal balance) {
        logger.info("Received PATCH request to update Account Balance with ID: {}, WITH New Balance: {}",
                accountName, balance);
        AccountDTO responseAccountDTO = accountService.updateAccountBalanceByAccountName(accountName, balance);
        logger.debug("Request was successfully processed and response was sent: {}", responseAccountDTO);
        return ResponseEntity.ok(responseAccountDTO);
    }

    @DeleteMapping("/by-account-id/{accountId}")
    public ResponseEntity<String> deleteAccountByAccountId(@PathVariable UUID accountId) {
        logger.info("Received DELETE request to remove Account with ID: {} ", accountId);
        ResponseEntity<String> responseMessage = accountService.deleteAccountByAccountId(accountId);
        logger.debug("Request was successfully processed and response message was sent: {}", responseMessage);
        return responseMessage;
    }

    @DeleteMapping("/by-account-name/{accountName}")
    public ResponseEntity<String> deleteAccountByAccountName(@PathVariable String accountName) {
        logger.info("Received DELETE request to remove Account with Name: {} ", accountName);
        ResponseEntity<String> responseMessage = accountService.deleteAccountByAccountName(accountName);
        logger.debug("Request was successfully processed and response message was sent: {}", responseMessage);
        return responseMessage;
    }

    @DeleteMapping("/by-user-id/{userId}")
    public ResponseEntity<String> deleteAllUserAccountsByUserId(@PathVariable UUID userId) {
        logger.info("Received DELETE request to remove All User Accounts with User ID: {} ", userId);
        ResponseEntity<String> responseMessage = accountService.deleteAllUserAccountsByUserId(userId);
        logger.debug("Request was successfully processed and response message was sent: {}", responseMessage);
        return responseMessage;
    }
}