package com.example.accountcomponent.service;

import com.example.accountcomponent.dto.AccountDTO;
import com.example.accountcomponent.dto.CardDTO;
import com.example.accountcomponent.dto.UsersDTO;
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
import java.util.stream.Collectors;

@Service
public class RestAccountServiceImpl implements RestAccountService {
    private static final Logger logger = LoggerFactory.getLogger(RestAccountServiceImpl.class);
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
        logger.info(ACCOUNT_SEARCHING_LOG, accountName);
        AccountDTO accountDTO = accountRepository.findByAccountName(accountName)
                .map(accountEntity -> {
                    logger.debug("Account was found in DB: {}", accountEntity);
                    return convertAccountModelToDTO(accountEntity);
                })
                .orElseThrow(() -> {
                    logger.error(ACCOUNT_NOT_FOUND_LOG, accountName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such name: " + accountName + " was not found");
                });
        logger.info("Trying to find account cards by accountId: {}", accountDTO.getId());
        List<CardDTO> cardDTOS = cardComponentClient.findAllCardsByAccountId(accountDTO.getId())
                .stream()
                .peek(cardDTO -> logger.info("Card was found and added to AccountDTO response: {}", cardDTO))
                .toList();
        accountDTO.setCards(cardDTOS);
        return accountDTO;
    }

    @Override
    @Transactional
    public AccountDTO getAccountById(UUID accountId) {
        logger.info(ACCOUNT_SEARCHING_LOG, accountId);
        AccountDTO accountDTO = accountRepository.findById(accountId)
                .map(accountEntity -> {
                    logger.debug("Account was found in DB: {}", accountEntity);
                    return convertAccountModelToDTO(accountEntity);
                })
                .orElseThrow(() -> {
                    logger.error(ACCOUNT_NOT_FOUND_LOG, accountId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found");
                });
        logger.info("Trying to find account cards by accountId: {}", accountDTO.getId());
        List<CardDTO> cardDTOS = cardComponentClient.findAllCardsByAccountId(accountDTO.getId())
                .stream()
                .peek(cardDTO -> logger.info("Card was found and added to AccountDTO response: {}", cardDTO))
                .toList();
        accountDTO.setCards(cardDTOS);
        return accountDTO;
    }

    @Override
    @Transactional
    public List<AccountDTO> getAllUserAccountsByUserId(UUID userId) {
        logger.info(USER_SEARCHING_LOG, userId);
        UsersDTO usersDTO = usersComponentClient.findById(userId)
                .orElseThrow(() -> {
                    logger.error(USER_NOT_FOUND_LOG, userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found: ");
                });
        logger.info(USER_FOUND_LOG, usersDTO);
        logger.info("Trying to find All Accounts linked to user with ID: {}", userId);
        List<Account> accounts = accountRepository.findByAccountHolderFullName(usersDTO.getFullName());
        logger.debug(ACCOUNTS_FOUND_LOG, accounts);
        return accounts.stream()
                .map(this::convertAccountModelToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<AccountDTO> getAllAccountsByHolderFullName(String accountHolderFullName) {
        logger.info(USER_SEARCHING_LOG, accountHolderFullName);
        UsersDTO usersDTO = usersComponentClient.findByFullName(accountHolderFullName).orElseThrow(() -> {
            logger.error(USER_NOT_FOUND_LOG, accountHolderFullName);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User with such Name: " + accountHolderFullName + " was not found: ");
        });
        logger.info(USER_FOUND_LOG, usersDTO);
        logger.info("Trying to find All Accounts linked to user with Name: {}", accountHolderFullName);
        List<Account> accounts = accountRepository.findByAccountHolderFullName(accountHolderFullName);
        logger.debug(ACCOUNTS_FOUND_LOG, accounts);
        return accounts.stream()
                .map(this::convertAccountModelToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getBalanceByAccountId(UUID accountId) {
        logger.info(ACCOUNT_SEARCHING_LOG, accountId);
        return accountRepository.findById(accountId)
                .map(accountEntity -> {
                    logger.debug("Account was found and it balance: {} received to the Controller",
                            accountEntity.getBalance());
                    return accountEntity.getBalance();
                })
                .orElseThrow(() -> {
                    logger.error(ACCOUNT_NOT_FOUND_LOG, accountId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found: ");
                });
    }

    @Override
    @Transactional
    public List<AccountDTO> getAllAccountsWithStatusByUserId(UUID userId, String accountStatus) {
        logger.info(USER_SEARCHING_LOG, userId);
        UsersDTO usersDTO = usersComponentClient.findById(userId)
                .orElseThrow(() -> {
                    logger.error(USER_NOT_FOUND_LOG, userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found: ");
                });
        logger.info(USER_FOUND_LOG, usersDTO);
        logger.info("Trying to find All Accounts linked to user with ID: {}", userId);
        List<Account> accounts = accountRepository.findByAccountHolderFullName(usersDTO.getFullName());
        return accounts.stream()
                .filter(account -> account.getStatus().equals(accountStatus))
                .map(filteredEntity -> {
                    logger.debug(ACCOUNTS_FOUND_LOG, filteredEntity);
                    return convertAccountModelToDTO(filteredEntity);
                })
                .collect(Collectors.toList());
    }
}