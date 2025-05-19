package com.example.accountcomponent.service;

import com.example.accountcomponent.dto.AccountDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface RestAccountService {

    AccountDTO getAccountByAccountName(String accountName);

    AccountDTO getAccountById(UUID accountId);

    List<AccountDTO> getAllUserAccountsByUserId(UUID userId);

    List<AccountDTO> getAllAccountsByHolderFullName(String accountHolderFullName);

    BigDecimal getBalanceByAccountId(UUID accountId);

    List<AccountDTO> getAllAccountsWithStatusByUserId(UUID userId, String accountStatus);

    boolean existsById(UUID accountId);
}
