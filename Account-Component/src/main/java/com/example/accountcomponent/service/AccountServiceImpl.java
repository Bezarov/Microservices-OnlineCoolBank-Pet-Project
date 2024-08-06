package com.example.accountcomponent.service;

import com.example.accountcomponent.dto.AccountDTO;
import com.example.accountcomponent.model.Account;
import com.example.accountcomponent.model.Users;
import com.example.accountcomponent.repository.AccountRepository;
import com.example.accountcomponent.repository.CardRepository;
import com.example.accountcomponent.repository.UsersRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
    private final AccountRepository accountRepository;
    private final UsersRepository usersRepository;
    private final CardRepository cardRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository,
                              UsersRepository usersRepository, CardRepository cardRepository) {
        this.accountRepository = accountRepository;
        this.usersRepository = usersRepository;
        this.cardRepository = cardRepository;
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
        accountDTO.setCards(account.getCards());
        accountDTO.setStatus(account.getStatus());
        return accountDTO;
    }

    private Account convertAccountDTOToModel(Users userEntity, AccountDTO accountDTO) {
        Account account = new Account();
        account.setAccountName(accountDTO.getAccountName());
        account.setBalance(accountDTO.getBalance());
        account.setAccountHolderFullName(userEntity.getFullName());
        account.setStatus(accountDTO.getStatus());
        account.setAccountType(accountDTO.getAccountType());
        account.setCreatedDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        account.setCurrency(accountDTO.getCurrency());
        account.setUser(userEntity);
        return account;
    }

    @Override
    public AccountDTO createAccount(UUID userId, AccountDTO accountDTO) {
        logger.info("Trying to find user with ID: {}", userId);
        Users userEntity = usersRepository.findById(userId).orElseThrow(() -> {
            logger.error("User with such ID: {}, was not found", userId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User with such ID: " + userId + " was not found");
        });
        logger.info("User was found successfully: {}", userEntity);

        logger.info("Trying to create Account: {}", accountDTO);
        Account account = accountRepository.save(convertAccountDTOToModel(userEntity, accountDTO));
        logger.debug("Account created successfully: {}", accountDTO);
        return convertAccountModelToDTO(account);
    }

    @Override
    public AccountDTO getAccountByAccountName(String accountName) {
        logger.info("Trying to find account with Name: {}", accountName);
        return accountRepository.findByAccountName(accountName)
                .map(AccountEntity -> {
                    logger.debug("Account was found and received to the Controller: {}", AccountEntity);
                    return convertAccountModelToDTO(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such name: {} was not found", accountName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such name: " + accountName + " was not found");
                });
    }

    @Override
    public AccountDTO getAccountById(UUID accountId) {
        logger.info("Trying to find Account with ID: {}", accountId);
        return accountRepository.findById(accountId)
                .map(AccountEntity -> {
                    logger.debug("Account was found and received to the Controller: {}", AccountEntity);
                    return convertAccountModelToDTO(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such ID: {} was not found", accountId);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Account with such ID: " + accountId + " was not found");
                });
    }

    @Override
    public List<AccountDTO> getAllUserAccountsByUserId(UUID userId) {
        logger.info("Trying to find user with ID: {}", userId);
        usersRepository.findById(userId).orElseThrow(() -> {
            logger.error("User with such ID: {} was not found", userId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User with such ID: " + userId + " was not found: ");
        });

        logger.info("Trying to find All Accounts linked to user with ID: {}", userId);
        List<Account> accounts = accountRepository.findAllByUsersId(userId);
        logger.debug("Accounts was found and received to the Controller: {}", accounts);
        return accounts.stream()
                .map(this::convertAccountModelToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountDTO> getAllAccountsByHolderFullName(String accountHolderFullName) {
        logger.info("Trying to find user with Name: {}", accountHolderFullName);
        usersRepository.findByFullName(accountHolderFullName).orElseThrow(() -> {
            logger.error("User with such Name: {} was not found", accountHolderFullName);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User with such full name: " + accountHolderFullName + " was not found");
        });

        logger.info("Trying to find All Accounts linked to user with Name: {}", accountHolderFullName);
        List<Account> accounts = accountRepository.findByAccountHolderFullName(accountHolderFullName);
        logger.debug("Accounts was found and received to the Controller: {}", accounts);
        return accounts.stream()
                .map(this::convertAccountModelToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getBalanceByAccountId(UUID accountId) {
        logger.info("Trying to find account with ID: {}", accountId);
        return accountRepository.findById(accountId)
                .map(AccountEntity -> {
                    logger.debug("Account was found and it balance: {} received to the Controller",
                            AccountEntity.getBalance());
                    return AccountEntity.getBalance();
                })
                .orElseThrow(() -> {
                    logger.error("Account with such ID: {} was not found", accountId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found: ");
                });
    }

    @Override
    public List<AccountDTO> getAllAccountsByStatus(UUID userId, String accountStatus) {
        logger.info("Trying to find User with ID: {}", userId);
        usersRepository.findById(userId).orElseThrow(() -> {
            logger.error("User with such ID: {} was not found", userId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User with such ID: " + userId + " was not found");
        });

        logger.info("Trying to find All Accounts linked to user with ID: {}", userId);
        List<Account> accounts = accountRepository.findAllByUsersId(userId);
        return accounts.stream()
                .filter(account -> account.getStatus().equals(accountStatus))
                .map(FilteredEntity -> {
                    logger.debug("Accounts was found and received to the Controller: {}", FilteredEntity);
                    return convertAccountModelToDTO(FilteredEntity);
                })
                .collect(Collectors.toList());
    }

    @Override
    public AccountDTO refillAccount(UUID accountId, BigDecimal amount) {
        logger.info("Trying to find Account with ID: {}", accountId);
        return accountRepository.findById(accountId)
                .map(AccountEntity -> {
                    AccountEntity.setBalance(AccountEntity.getBalance().add(amount));
                    accountRepository.save(AccountEntity);
                    logger.debug("Account was found and balance was refilled successfully," +
                            " received to the Controller: {}", AccountEntity.getBalance());
                    return convertAccountModelToDTO(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such ID: {} was not found", accountId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found");
                });
    }

    @Override
    public AccountDTO updateAccountById(UUID accountId, AccountDTO accountDTO) {
        logger.info("Trying to find Account with ID: {}", accountId);
        return accountRepository.findById(accountId)
                .map(AccountEntity -> {
                    AccountEntity.setAccountName(accountDTO.getAccountName());
                    AccountEntity.setStatus(accountDTO.getStatus());
                    AccountEntity.setAccountType(accountDTO.getAccountType());
                    AccountEntity.setCurrency(accountDTO.getCurrency());
                    accountRepository.save(AccountEntity);
                    logger.debug("Account updated successfully " +
                            "and received to the Controller: {}", AccountEntity);
                    return convertAccountModelToDTO(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such ID: {} was not found", accountId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found");
                });
    }

    @Override
    public AccountDTO updateAccountStatusById(UUID accountId, String status) {
        logger.info("Trying to find Account with ID: {}", accountId);
        return accountRepository.findById(accountId)
                .map(AccountEntity -> {
                    AccountEntity.setStatus(status);
                    accountRepository.save(AccountEntity);
                    logger.debug("Account Status updated successfully " +
                            "and received to the Controller: {}", AccountEntity);
                    return convertAccountModelToDTO(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such ID: {} was not found", accountId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found");
                });
    }

    @Override
    public AccountDTO updateAccountBalanceById(UUID accountId, BigDecimal newBalance) {
        logger.info("Trying to find Account with ID: {}", accountId);
        return accountRepository.findById(accountId)
                .map(AccountEntity -> {
                    AccountEntity.setBalance(newBalance);
                    accountRepository.save(AccountEntity);
                    logger.debug("Account Balance updated successfully " +
                            "and received to the Controller: {}", AccountEntity);
                    return convertAccountModelToDTO(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such ID: {} was not found", accountId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found");
                });
    }

    @Override
    public AccountDTO updateAccountBalanceByAccountName(String accountName, BigDecimal balance) {
        logger.info("Trying to find Account with Name: {}", accountName);
        return accountRepository.findByAccountName(accountName)
                .map(AccountEntity -> {
                    AccountEntity.setBalance(balance);
                    accountRepository.save(AccountEntity);
                    logger.info("Account balance with name {}, updated successfully " +
                                    "and received to the Controller {}",
                            accountName, AccountEntity);
                    return convertAccountModelToDTO(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such Name: {} was not found", accountName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such Name: " + accountName + " was not found");
                });
    }

    @Transactional
    @Override
    public ResponseEntity<String> deleteAccountByAccountId(UUID accountId) {
        logger.info("Trying to find Account with ID: {}", accountId);
        accountRepository.findById(accountId)
                .map(AccountEntity -> {
                    AccountEntity.setStatus("PRE-REMOVED");
                    logger.debug("Account Status was changed to - PRE-REMOVED: {}", AccountEntity);
                    return accountRepository.save(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such ID: {} was not found", accountId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found");
                });

        logger.info("Account was found, Trying to find All Account Cards with Account ID: {}", accountId);
        cardRepository.findAllByAccountId(accountId).forEach(CardEntity -> {
            CardEntity.setStatus("DEACTIVATED");
            cardRepository.save(CardEntity);
            logger.debug("All found Account Cards Status was changed to - DEACTIVATED: {}", CardEntity);
        });
        return new ResponseEntity<>("Account deleted successfully", HttpStatus.ACCEPTED);
    }

    @Transactional
    @Override
    public ResponseEntity<String> deleteAccountByAccountName(String accountName) {
        logger.info("Trying to find Account with Name: {}", accountName);
        accountRepository.findByAccountName(accountName)
                .map(AccountEntity -> {
                    AccountEntity.setStatus("PRE-REMOVED");
                    logger.debug("Account Status was changed to - PRE-REMOVED: {}", AccountEntity);
                    return accountRepository.save(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such Name: {} was not found", accountName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such Account Name: " + accountName + " was not found");
                });

        logger.info("Account was found, Trying to find All Account Cards with Account Name: {}", accountName);
        cardRepository.findAllByAccountId(accountRepository.findByAccountName(accountName).get().getId())
                .forEach(EntityCard -> {
                    EntityCard.setStatus("DEACTIVATED");
                    logger.debug("All found Account Cards Status was changed to - DEACTIVATED");
                    cardRepository.save(EntityCard);
                });
        return new ResponseEntity<>("Account deleted successfully", HttpStatus.ACCEPTED);
    }

    @Transactional
    @Override
    public ResponseEntity<String> deleteAllUserAccountsByUserId(UUID userId) {
        logger.info("Trying to find User with User ID: {}", userId);
        usersRepository.findById(userId).orElseThrow(() -> {
            logger.error("User with such ID: {} was not found", userId);
            return new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User with such ID: " + userId + " was not found ");
        });

        logger.info("User was found, Trying to find All User Accounts with User ID: {}", userId);
        accountRepository.findAllByUsersId(userId).forEach(AccountEntity -> {
            AccountEntity.setStatus("PRE-REMOVED");
            logger.debug("Account Status was changed to - PRE-REMOVED: {}", AccountEntity);

            logger.info("Trying to find All Account Cards with Account ID: {}", AccountEntity.getId());
            cardRepository.findAllByAccountId(AccountEntity.getId()).forEach(CardEntity -> {
                CardEntity.setStatus("DEACTIVATED");
                logger.debug("Card Status was changed to - PRE-REMOVED: {}", CardEntity);
                cardRepository.save(CardEntity);
            });
            accountRepository.save(AccountEntity);
        });
        return new ResponseEntity<>("Accounts deleted successfully", HttpStatus.ACCEPTED);
    }
}