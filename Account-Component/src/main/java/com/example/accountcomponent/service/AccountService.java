package com.example.accountcomponent.service;

import com.example.accountcomponent.dto.AccountDTO;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {
    AccountDTO createAccount(UUID userId, AccountDTO accountDTO);

    AccountDTO getAccountByAccountName(String accountName);

    AccountDTO getAccountById(UUID accountId);

    List<AccountDTO> getAllUserAccountsByUserId(UUID userId);

    List<AccountDTO> getAllAccountsByHolderFullName(String accountHolderFullName);

    BigDecimal getBalanceByAccountId(UUID accountId);

    List<AccountDTO> getAllAccountsByStatus(UUID userId, String accountStatus);

    AccountDTO refillAccount(UUID accountId, BigDecimal amount);

    AccountDTO updateAccountById(UUID accountId, AccountDTO accountDTO);

    AccountDTO updateAccountStatusById(UUID accountId, String status);

    AccountDTO updateAccountBalanceById(UUID accountId, BigDecimal newBalance);

    AccountDTO updateAccountBalanceByAccountName(String accountName, BigDecimal balance);

    ResponseEntity<String> deleteAccountByAccountId(UUID accountId);

    ResponseEntity<String> deleteAccountByAccountName(String accountName);

    ResponseEntity<String> deleteAllUserAccountsByUserId(UUID userId);

}
