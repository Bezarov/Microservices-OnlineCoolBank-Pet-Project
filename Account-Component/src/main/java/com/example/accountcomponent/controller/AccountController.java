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
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);
    private static final String RESPONSE_LOG = "Request was successfully processed and response was sent: {}";

    private final RestAccountService restAccountService;

    @Autowired
    public AccountController(RestAccountService restAccountService) {
        this.restAccountService = restAccountService;
    }

    @GetMapping("/by-account-id/{accountId}")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable UUID accountId) {
        LOGGER.debug("Received GET request to get Account by ID: {}", accountId);
        AccountDTO responseAccountDTO = restAccountService.getAccountById(accountId);
        LOGGER.debug(RESPONSE_LOG, responseAccountDTO);
        return ResponseEntity.ok(responseAccountDTO);
    }


    @GetMapping("/by-holder-name/{accountHolderFullName}")
    public ResponseEntity<List<AccountDTO>> getAllAccountsByHolderFullName(@PathVariable String accountHolderFullName) {
        LOGGER.debug("Received GET request to get All User Accounts by HOLDER FULL NAME: {}",
                accountHolderFullName);
        List<AccountDTO> responseAccountDTOS = restAccountService.getAllAccountsByHolderFullName(accountHolderFullName);
        LOGGER.debug(RESPONSE_LOG, responseAccountDTOS);
        return ResponseEntity.ok(responseAccountDTOS);
    }

    @GetMapping("/by-account-id/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalanceByAccountId(@PathVariable UUID accountId) {
        LOGGER.debug("Received GET request to get Account balance by ACCOUNT ID: {}", accountId);
        BigDecimal responseBigDecimal = restAccountService.getBalanceByAccountId(accountId);
        LOGGER.debug(RESPONSE_LOG, responseBigDecimal);
        return ResponseEntity.ok(responseBigDecimal);
    }

    @GetMapping("/by-account-name/{accountName}")
    public ResponseEntity<AccountDTO> getAccountByAccountName(@PathVariable String accountName) {
        LOGGER.debug("Received GET request to get Account by ACCOUNT NAME: {}", accountName);
        AccountDTO responseAccountDTO = restAccountService.getAccountByAccountName(accountName);
        LOGGER.debug(RESPONSE_LOG, responseAccountDTO);
        return ResponseEntity.ok(responseAccountDTO);
    }

    @GetMapping("/by-user-id/{userId}")
    public ResponseEntity<List<AccountDTO>> getAllUserAccountsByUserId(@PathVariable UUID userId) {
        LOGGER.debug("Received GET request to get All User Accounts by USER ID: {}", userId);
        List<AccountDTO> responseAccountDTOS = restAccountService.getAllUserAccountsByUserId(userId);
        LOGGER.debug(RESPONSE_LOG, responseAccountDTOS);
        return ResponseEntity.ok(responseAccountDTOS);
    }

    @GetMapping("/by-user-id/{userId}/status")
    public ResponseEntity<List<AccountDTO>> getAccountsByStatus(@PathVariable UUID userId,
                                                                @RequestParam(name = "status") String accountStatus) {
        LOGGER.debug("Received GET request to get All User (USER ID: {}), Accounts by ACCOUNT STATUS: {}",
                userId, accountStatus);
        List<AccountDTO> responseAccountDTOS = restAccountService.getAllAccountsWithStatusByUserId(userId, accountStatus);
        LOGGER.debug(RESPONSE_LOG, responseAccountDTOS);
        return ResponseEntity.ok(responseAccountDTOS);
    }
}