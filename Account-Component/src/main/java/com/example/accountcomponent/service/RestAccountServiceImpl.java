package com.example.accountcomponent.service;

import com.example.accountcomponent.dto.AccountDTO;
import com.example.accountcomponent.exception.CustomKafkaException;
import com.example.accountcomponent.feign.CardComponentClient;
import com.example.accountcomponent.feign.UsersComponentClient;
import com.example.accountcomponent.model.Account;
import com.example.accountcomponent.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class RestAccountServiceImpl implements RestAccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAccountServiceImpl.class);
    private static final String ACCOUNT_SEARCHING_LOG = "Trying to find Account by: {}";
    private static final String ACCOUNT_NOT_FOUND_LOG = "Account was not found by: {}";
    private static final String USER_SEARCHING_LOG = "Trying to find User by: {}";
    private static final String USER_NOT_FOUND_LOG = "User was not found by: {}";
    private static final String USER_FOUND_LOG = "User was found successfully: {}";
    private static final String ACCOUNTS_FOUND_LOG = "Accounts was found and received to the Controller: {}";

    private final AccountRepository accountRepository;
    private final CardComponentClient cardComponentClient;
    private final UsersComponentClient usersComponentClient;

    public RestAccountServiceImpl(AccountRepository accountRepository,
                                  @Qualifier("Card-Components") CardComponentClient cardComponentClient,
                                  @Qualifier("Users-Components") UsersComponentClient usersComponentClient) {
        this.accountRepository = accountRepository;
        this.cardComponentClient = cardComponentClient;
        this.usersComponentClient = usersComponentClient;
    }

    private AccountDTO convertAccountModelToDTO(Account account) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(account.getId());
        accountDTO.setAccountName(account.getAccountName());
        accountDTO.setAccountHolderFullName(account.getAccountHolderFullName());
        accountDTO.setAccountType(account.getAccountType());
        accountDTO.setCreatedDate(account.getCreatedDate());
        accountDTO.setBalance(account.getBalance());
        accountDTO.setCurrency(account.getCurrency());
        accountDTO.setStatus(account.getStatus());
        return accountDTO;
    }

    @Override
    @Transactional
    public AccountDTO getAccountByAccountName(String accountName) {
        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountName);
        return accountRepository.findByAccountName(accountName)
                .map(accountEntity -> {
                    AccountDTO accountDTO = convertAccountModelToDTO(accountEntity);
                    LOGGER.debug("Account was found: {}", accountEntity);
                    LOGGER.debug("Trying to find it Cards by ID: {}", accountEntity.getId());
                    accountDTO.setCards(cardComponentClient.findAllCardsByAccountId(accountEntity.getId()));
                    return accountDTO;
                })
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such name: " + accountName + " was not found");
                });
    }

    @Override
    @Transactional
    public AccountDTO getAccountById(UUID accountId) {
        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountId);
        return accountRepository.findById(accountId)
                .map(accountEntity -> {
                    AccountDTO accountDTO = convertAccountModelToDTO(accountEntity);
                    LOGGER.debug("Account was found: {}", accountEntity);
                    LOGGER.debug("Trying to find it Cards by ID: {}", accountEntity.getId());
                    accountDTO.setCards(cardComponentClient.findAllCardsByAccountId(accountEntity.getId()));
                    return accountDTO;
                })
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found");
                });
    }

    @Override
    @Transactional
    public List<AccountDTO> getAllUserAccountsByUserId(UUID userId) {
        String userName = getUserNameByUserId(userId);
        LOGGER.info(USER_FOUND_LOG, userName);

        LOGGER.info("Trying to find All Accounts linked to user with ID: {}", userId);
        List<Account> accounts = accountRepository.findByAccountHolderFullName(userName);
        LOGGER.debug(ACCOUNTS_FOUND_LOG, accounts);
        return accounts.stream()
                .map(this::convertAccountModelToDTO)
                .toList();
    }

    @Override
    @Transactional
    public List<AccountDTO> getAllAccountsByHolderFullName(String accountHolderFullName) {
        LOGGER.info(USER_SEARCHING_LOG, accountHolderFullName);
        usersComponentClient.findByFullName(accountHolderFullName)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, accountHolderFullName);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such Full name: " + accountHolderFullName + " was not found");
                });
        LOGGER.debug("User existence by Full name: {} check successfully", accountHolderFullName);

        LOGGER.info("Trying to find All Accounts linked to user with Name: {}", accountHolderFullName);
        List<Account> accounts = accountRepository.findByAccountHolderFullName(accountHolderFullName);
        LOGGER.debug(ACCOUNTS_FOUND_LOG, accounts);
        return accounts.stream()
                .map(this::convertAccountModelToDTO)
                .toList();
    }

    @Override
    public BigDecimal getBalanceByAccountId(UUID accountId) {
        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountId);
        return accountRepository.findAccountBalanceById(accountId)
                .map(balance -> {
                    LOGGER.debug("Account was found and it balance: {} received to the Controller", balance);
                    return balance;
                })
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found: ");
                });
    }

    @Override
    @Transactional
    public List<AccountDTO> getAllAccountsWithStatusByUserId(UUID userId, String accountStatus) {
        String userName = getUserNameByUserId(userId);
        LOGGER.info(USER_FOUND_LOG, userName);

        LOGGER.info("Trying to find All Accounts linked to user with ID: {}", userId);
        return accountRepository.findByAccountHolderFullName(userName).stream()
                .filter(account -> account.getStatus().equals(accountStatus))
                .map(filteredEntity -> {
                    LOGGER.debug(ACCOUNTS_FOUND_LOG, filteredEntity);
                    return convertAccountModelToDTO(filteredEntity);
                })
                .toList();
    }

    @Override
    public boolean existsById(UUID accountId) {
        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountId);
        return accountRepository.existsById(accountId);
    }

    private String getUserNameByUserId(UUID userId) {
        LOGGER.info(USER_SEARCHING_LOG, userId);
        return usersComponentClient.findFullNameById(userId)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found: ");
                });
    }
}