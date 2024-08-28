package com.example.accountcomponent.controller;

import com.example.accountcomponent.dto.AccountDTO;
import com.example.accountcomponent.service.RestAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final RestAccountService restAccountService;

    @Autowired
    public AccountController(RestAccountService restAccountService) {
        this.restAccountService = restAccountService;
    }

    @GetMapping("/by-account-id/{accountId}")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable UUID accountId) {
        logger.info("Received GET request to get Account by ID: {}", accountId);
        AccountDTO responseAccountDTO = restAccountService.getAccountById(accountId);
        logger.debug("Request was successfully processed and response was sent: {}", responseAccountDTO);
        return ResponseEntity.ok(responseAccountDTO);
    }


    @GetMapping("/by-holder-name/{accountHolderFullName}")
    public ResponseEntity<List<AccountDTO>> getAllAccountsByHolderFullName(@PathVariable
                                                                           String accountHolderFullName) {
        logger.info("Received GET request to get All User Accounts by HOLDER FULL NAME: {}",
                accountHolderFullName);
        List<AccountDTO> responseAccountDTOS = restAccountService.getAllAccountsByHolderFullName(accountHolderFullName);
        logger.debug("Request was successfully processed and response was sent: {}", responseAccountDTOS);
        return ResponseEntity.ok(responseAccountDTOS);
    }

    @GetMapping("/by-account-id/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalanceByAccountId(@PathVariable UUID accountId) {
        logger.info("Received GET request to get Account balance by ACCOUNT ID: {}", accountId);
        BigDecimal responseBigDecimal = restAccountService.getBalanceByAccountId(accountId);
        logger.debug("Request was successfully processed and response was sent: {}", responseBigDecimal);
        return ResponseEntity.ok(responseBigDecimal);
    }

    @GetMapping("/by-account-name/{accountName}")
    public ResponseEntity<AccountDTO> getAccountByAccountName(@PathVariable String accountName) {
        logger.info("Received GET request to get Account by ACCOUNT NAME: {}", accountName);
        AccountDTO responseAccountDTO = restAccountService.getAccountByAccountName(accountName);
        logger.debug("Request was successfully processed and response was sent: {}", responseAccountDTO);
        return ResponseEntity.ok(responseAccountDTO);
    }

    @GetMapping("/by-user-id/{userId}")
    public ResponseEntity<List<AccountDTO>> getAllUserAccountsByUserId(@PathVariable UUID userId) {
        logger.info("Received GET request to get All User Accounts by USER ID: {}", userId);
        List<AccountDTO> responseAccountDTOS = restAccountService.getAllUserAccountsByUserId(userId);
        logger.debug("Request was successfully processed and response was sent: {}", responseAccountDTOS);
        return ResponseEntity.ok(responseAccountDTOS);
    }

    @GetMapping("/by-user-id/{userId}/status")
    public ResponseEntity<List<AccountDTO>> getAccountsByStatus(
            @PathVariable UUID userId, @RequestParam(name = "status") String accountStatus) {
        logger.info("Received GET request to get All User (USER ID: {}), Accounts by ACCOUNT STATUS: {}",
                userId, accountStatus);
        List<AccountDTO> responseAccountDTOS = restAccountService.getAllAccountsWithStatusByUserId(userId, accountStatus);
        logger.debug("Request was successfully processed and response was sent: {}", responseAccountDTOS);
        return ResponseEntity.ok(responseAccountDTOS);
    }
}