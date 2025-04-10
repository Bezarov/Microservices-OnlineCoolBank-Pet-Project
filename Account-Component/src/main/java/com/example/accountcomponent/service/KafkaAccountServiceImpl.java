package com.example.accountcomponent.service;

import com.example.accountcomponent.dto.AccountDTO;
import com.example.accountcomponent.dto.CardDTO;
import com.example.accountcomponent.dto.UsersDTO;
import com.example.accountcomponent.exception.CustomKafkaException;
import com.example.accountcomponent.feign.CardComponentClient;
import com.example.accountcomponent.feign.UsersComponentClient;
import com.example.accountcomponent.model.Account;
import com.example.accountcomponent.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KafkaAccountServiceImpl implements KafkaAccountService {
    private final static Logger logger = LoggerFactory.getLogger(KafkaAccountServiceImpl.class);
    private final AccountRepository accountRepository;
    private final KafkaTemplate<String, AccountDTO> responseDTOKafkaTemplate;
    private final KafkaTemplate<String, List<AccountDTO>> responseDTOSKafkaTemplate;
    private final KafkaTemplate<String, String> responseMessageKafkaTemplate;
    private final KafkaTemplate<String, BigDecimal> responseBigDecimalKafkaTemplate;
    private final UsersComponentClient usersComponentClient;
    private final CardComponentClient cardComponentClient;

    public KafkaAccountServiceImpl(AccountRepository accountRepository, KafkaTemplate<String,
            AccountDTO> responseDTOKafkaTemplate, KafkaTemplate<String, List<AccountDTO>> responseDTOSKafkaTemplate,
                                   KafkaTemplate<String, String> responseMessageKafkaTemplate,
                                   KafkaTemplate<String, BigDecimal> responseBigDecimalKafkaTemplate,
                                   @Qualifier("Users-Components") UsersComponentClient usersComponentClient,
                                   @Qualifier("Card-Components") CardComponentClient cardComponentClient) {
        this.accountRepository = accountRepository;
        this.responseDTOKafkaTemplate = responseDTOKafkaTemplate;
        this.responseDTOSKafkaTemplate = responseDTOSKafkaTemplate;
        this.responseMessageKafkaTemplate = responseMessageKafkaTemplate;
        this.responseBigDecimalKafkaTemplate = responseBigDecimalKafkaTemplate;
        this.usersComponentClient = usersComponentClient;
        this.cardComponentClient = cardComponentClient;
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

    private Account convertAccountDTOToModel(UsersDTO userDTO, AccountDTO accountDTO) {
        Account account = new Account();
        account.setAccountName(accountDTO.getAccountName());
        account.setBalance(accountDTO.getBalance());
        account.setAccountHolderFullName(userDTO.getFullName());
        account.setStatus(accountDTO.getStatus());
        account.setAccountType(accountDTO.getAccountType());
        account.setCreatedDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        account.setCurrency(accountDTO.getCurrency());
        return account;
    }

    @Override
    @KafkaListener(topics = "create-account-by-user-id", groupId = "account-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void createAccount(Map<String, AccountDTO> userIdToAccountDTOMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: create-account-by-user-id with correlation id: {} ", correlationId);
        ObjectMapper objectMapper = new ObjectMapper();
        String userId = userIdToAccountDTOMap.keySet().iterator().next().replaceAll("\"", "");
        AccountDTO accountDTO = objectMapper.convertValue(userIdToAccountDTOMap.get(userId), AccountDTO.class);

        logger.info("Trying to find user with ID: {}", userId);
        UsersDTO userDTO = usersComponentClient.findById(UUID.fromString(userId))
                .orElseThrow(() -> {
                    logger.error("User with such ID was not found: {}", userId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });
        logger.info("User was found successfully: {}", userId);
        logger.info("Trying to create Account: {}", accountDTO);
        accountRepository.findByAccountName(accountDTO.getAccountName())
                .ifPresent(AccountEntity -> {
                    logger.error("Account with such name already exists: {}", AccountEntity);
                    throw new CustomKafkaException(HttpStatus.FOUND, "Account with such name: " +
                            accountDTO.getAccountName() + " already exists correlationId:" + correlationId);
                });
        Account account = accountRepository.save(convertAccountDTOToModel(userDTO, accountDTO));
        logger.debug("Account created successfully: {}", accountDTO);

        logger.info("Trying to create topic: create-account-by-user-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "create-card-by-account-id-response", null, convertAccountModelToDTO(account));
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-account-by-account-name", groupId = "account-component",
            containerFactory = "stringKafkaListenerFactory")
    public void getAccountByAccountName(String accountName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-account-by-account-name with correlation id: {} ", correlationId);
        logger.info("Trying to find account with Name: {}", accountName);
        AccountDTO accountDTO = accountRepository.findByAccountName(accountName)
                .map(AccountEntity -> {
                    logger.debug("Account was found in DB: {}", AccountEntity);
                    return convertAccountModelToDTO(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such name was not found: {} ", accountName);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such name: " + accountName + " was not found correlationId:" + correlationId);
                });
        logger.info("Trying to find account cards by accountId: {}", accountDTO.getId());
        List<CardDTO> cardDTOS = cardComponentClient.findAllCardsByAccountId(accountDTO.getId())
                .stream()
                .peek(CardDTO -> logger.info("Card was found and added to AccountDTO response: {}", CardDTO))
                .toList();
        accountDTO.setCards(cardDTOS);

        logger.info("Trying to create topic: get-account-by-account-name-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "get-account-by-account-name-response", null, accountDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-account-by-account-id", groupId = "account-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getAccountById(UUID accountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-account-by-account-id with correlation id: {} ", correlationId);
        logger.info("Trying to find Account with ID: {}", accountId);
        AccountDTO accountDTO = accountRepository.findById(accountId)
                .map(AccountEntity -> {
                    logger.debug("Account was found in DB: {}", AccountEntity);
                    return convertAccountModelToDTO(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such ID was not found: {}", accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found correlationId:" + correlationId);
                });
        logger.info("Trying to find account cards by accountId: {}", accountDTO.getId());
        List<CardDTO> cardDTOS = cardComponentClient.findAllCardsByAccountId(accountDTO.getId())
                .stream()
                .peek(CardDTO -> logger.info("Card was found and added to AccountDTO response: {}", CardDTO))
                .toList();
        accountDTO.setCards(cardDTOS);
        logger.info("Trying to create topic: get-account-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "get-account-by-account-id-response", null, accountDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-accounts-by-user-id", groupId = "account-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getAllUserAccountsByUserId(UUID userId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-all-accounts-by-user-id with correlation id: {} ", correlationId);
        logger.info("Trying to find user with ID: {}", userId);
        UsersDTO usersDTO = usersComponentClient.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User with such ID was not found: {}", userId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });
        logger.info("User was found successfully: {} \nTrying to find All Accounts linked to user with ID: {}",
                usersDTO, userId);

        List<AccountDTO> accountDTOS = accountRepository.findByAccountHolderFullName(usersDTO.getFullName())
                .stream()
                .map(this::convertAccountModelToDTO)
                .toList();
        logger.debug("Accounts was found: {} \nTrying to find it cards", accountDTOS);
        accountDTOS.forEach(AccountDTO -> {
            logger.debug("Trying to find account by id: {}, cards", AccountDTO.getId());
            cardComponentClient.findAllCardsByAccountId(AccountDTO.getId());
        });

        logger.info("Trying to create topic: get-all-accounts-by-user-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<AccountDTO>> responseTopic = new ProducerRecord<>(
                "get-account-by-account-id-response", null, accountDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-accounts-by-holder-full-name", groupId = "account-component",
            containerFactory = "stringKafkaListenerFactory")
    public void getAllAccountsByHolderFullName(String accountHolderFullName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-all-accounts-by-holder-full-name with correlation id: {} ", correlationId);
        logger.info("Trying to find user with full-name: {}", accountHolderFullName);
        UsersDTO usersDTO = usersComponentClient.findByFullName(accountHolderFullName)
                .orElseThrow(() -> {
                    logger.error("User with such full-name: {} was not found", accountHolderFullName);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such full-name: " + accountHolderFullName + " was not found correlationId:" + correlationId);
                });
        logger.info("User was found successfully: {} \nTrying to find All Accounts linked to user with ID: {}",
                usersDTO, usersDTO.getId());

        List<AccountDTO> accountDTOS = accountRepository.findByAccountHolderFullName(usersDTO.getFullName())
                .stream()
                .map(this::convertAccountModelToDTO)
                .toList();
        logger.debug("Accounts was found: {} \nTrying to find it cards", accountDTOS);
        accountDTOS.forEach(AccountDTO -> {
            logger.debug("Trying to find account by id: {}, cards", AccountDTO.getId());
            cardComponentClient.findAllCardsByAccountId(AccountDTO.getId());
        });

        logger.info("Trying to create topic: get-all-accounts-by-holder-full-name-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<AccountDTO>> responseTopic = new ProducerRecord<>(
                "get-all-accounts-by-holder-full-name-response", null, accountDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-balance-by-account-id", groupId = "account-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getBalanceByAccountId(UUID accountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-balance-by-account-id with correlation id: {} ", correlationId);
        logger.info("Trying to find account with ID: {}", accountId);
        BigDecimal balance = accountRepository.findById(accountId)
                .map(AccountEntity -> {
                    logger.debug("Account was found and it balance successfully: {}", AccountEntity.getBalance());
                    return AccountEntity.getBalance();
                })
                .orElseThrow(() -> {
                    logger.error("Account with such ID: {} was not found", accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found correlationId:" + correlationId);
                });

        logger.info("Trying to create topic: get-balance-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, BigDecimal> responseTopic = new ProducerRecord<>(
                "get-balance-by-account-id-response", null, balance);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseBigDecimalKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-accounts-by-status", groupId = "account-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void getAllAccountsWithStatusByUserId(Map<String, String> userIdToAccountStatusMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-all-accounts-by-status with correlation id: {} ", correlationId);
        String userId = userIdToAccountStatusMap.keySet().iterator().next().replaceAll("\"", "");
        String accountStatus = userIdToAccountStatusMap.get(userId);
        logger.info("Trying to find User with ID: {}", userId);
        UsersDTO usersDTO = usersComponentClient.findById(UUID.fromString(userId)).orElseThrow(() -> {
            logger.error("User with such ID was not found: {}", userId);
            return new CustomKafkaException(HttpStatus.NOT_FOUND,
                    "User with such ID: " + userId + " was not found correlationId:" + correlationId);
        });
        logger.info("User was found successfully: {}", usersDTO);

        logger.info("Trying to find All Accounts linked to user with ID: {}, with status: {}", userId, accountStatus);
        List<AccountDTO> accountDTOS = accountRepository.findByAccountHolderFullName(usersDTO.getFullName())
                .stream()
                .filter(account -> account.getStatus().equals(accountStatus))
                .map(this::convertAccountModelToDTO)
                .toList();
        logger.info("Found Accounts with status: {}, in quantity: {} ", accountStatus, accountDTOS.size());
        logger.info("Trying to create topic: get-all-accounts-by-status-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<AccountDTO>> responseTopic = new ProducerRecord<>(
                "get-all-accounts-by-status-response", null, accountDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "refill-account-by-account-id", groupId = "account-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void refillAccount(Map<String, BigDecimal> accountIdToAmountMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: refill-account-by-account-id with correlation id: {} ", correlationId);
        String accountId = accountIdToAmountMap.keySet().iterator().next().replaceAll("\"", "");
        BigDecimal amount = accountIdToAmountMap.get(accountId);

        logger.info("Trying to find Account with ID: {}", accountId);
        AccountDTO accountDTO = accountRepository.findById(UUID.fromString(accountId))
                .map(AccountEntity -> {
                    AccountEntity.setBalance(AccountEntity.getBalance().add(amount));
                    accountRepository.save(AccountEntity);
                    logger.debug("Account was found and balance was refilled successfully: {}", AccountEntity.getBalance());
                    return convertAccountModelToDTO(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such ID: {} was not found", accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found correlationId:" + correlationId);
                });

        logger.info("Trying to create topic: refill-account-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "refill-account-by-account-id-response", null, accountDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "update-account-by-account-id", groupId = "account-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void updateAccountById(Map<String, AccountDTO> accountIdToAccountDTOMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: update-account-by-account-id with correlation id: {} ", correlationId);
        String accountId = accountIdToAccountDTOMap.keySet().iterator().next().replaceAll("\"", "");
        AccountDTO accountDTO = accountIdToAccountDTOMap.get(accountId);

        logger.info("Trying to find Account with ID: {}", accountId);
        AccountDTO responseAccountDTO = accountRepository.findById(UUID.fromString(accountId))
                .map(AccountEntity -> {
                    AccountEntity.setAccountName(accountDTO.getAccountName());
                    AccountEntity.setStatus(accountDTO.getStatus());
                    AccountEntity.setAccountType(accountDTO.getAccountType());
                    AccountEntity.setCurrency(accountDTO.getCurrency());
                    accountRepository.save(AccountEntity);
                    logger.debug("Account updated successfully: {}", AccountEntity);
                    return convertAccountModelToDTO(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such ID: {} was not found", accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found correlationId:" + correlationId);
                });

        logger.info("Trying to create topic: update-account-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "update-account-by-account-id-response", null, responseAccountDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "update-account-status-by-account-id", groupId = "account-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void updateAccountStatusById(Map<String, String> accountIdToStatusMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: update-account-status-by-account-id with correlation id: {} ", correlationId);
        String accountId = accountIdToStatusMap.keySet().iterator().next().replaceAll("\"", "");
        String accountStatus = accountIdToStatusMap.get(accountId);

        logger.info("Trying to find Account with ID: {}", accountId);
        AccountDTO accountDTO = accountRepository.findById(UUID.fromString(accountId))
                .map(AccountEntity -> {
                    AccountEntity.setStatus(accountStatus);
                    accountRepository.save(AccountEntity);
                    logger.debug("Account Status updated successfully: {}", AccountEntity);
                    return convertAccountModelToDTO(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such ID: {} was not found", accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found correlationId:" + correlationId);
                });

        logger.info("Trying to create topic: update-account-status-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "get-account-by-account-name-response", null, accountDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "update-account-status-by-account-id", groupId = "account-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void updateAccountBalanceById(Map<String, BigDecimal> accountIdToNewBalanceMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: update-account-status-by-account-id with correlation id: {} ", correlationId);
        String accountId = accountIdToNewBalanceMap.keySet().iterator().next().replaceAll("\"", "");
        BigDecimal newBalance = accountIdToNewBalanceMap.get(accountId);

        logger.info("Trying to find Account with ID: {}", accountId);
        AccountDTO accountDTO = accountRepository.findById(UUID.fromString(accountId))
                .map(AccountEntity -> {
                    AccountEntity.setBalance(newBalance);
                    accountRepository.save(AccountEntity);
                    logger.debug("Account Balance updated successfully: {}", AccountEntity);
                    return convertAccountModelToDTO(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such ID: {} was not found", accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found correlationId:" + correlationId);
                });

        logger.info("Trying to create topic: update-account-status-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "update-account-status-by-account-id-response", null, accountDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "update-account-balance-by-account-id", groupId = "account-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void updateAccountBalanceByAccountName(Map<String, BigDecimal> accountNameToNewBalanceMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: update-account-balance-by-account-id with correlation id: {} ", correlationId);
        String accountName = accountNameToNewBalanceMap.keySet().iterator().next().replaceAll("\"", "");
        BigDecimal newBalance = accountNameToNewBalanceMap.get(accountName);

        logger.info("Trying to find Account with Name: {}", accountName);
        AccountDTO accountDTO = accountRepository.findByAccountName(accountName)
                .map(AccountEntity -> {
                    AccountEntity.setBalance(newBalance);
                    accountRepository.save(AccountEntity);
                    logger.info("Account balance with name: {}, updated successfully: {}", accountName, AccountEntity);
                    return convertAccountModelToDTO(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such Name: {} was not found", accountName);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such Name: " + accountName + " was not found correlationId:" + correlationId);
                });

        logger.info("Trying to create topic: update-account-balance-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "update-account-balance-by-account-id-response", null, accountDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "delete-account-by-account-id", groupId = "account-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void deleteAccountByAccountId(UUID accountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: delete-account-by-account-id with correlation id: {} ", correlationId);
        logger.info("Trying to find Account with ID: {}", accountId);
        accountRepository.findById(accountId)
                .map(AccountEntity -> {
                    AccountEntity.setStatus("PRE-REMOVED");
                    logger.debug("Account Status was changed to - PRE-REMOVED: {}", AccountEntity);
                    return accountRepository.save(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such ID: {} was not found", accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found correlationId:" + correlationId);
                });
        logger.info("Account was found, Trying to find All Account Cards with Account ID: {}", accountId);

        cardComponentClient.deleteAllAccountCardsByAccountId(accountId);
        logger.debug("All found account Cards Status was changed to - DEACTIVATED");

        logger.info("Trying to create topic: delete-account-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "delete-account-by-account-id-response", null, "Account deleted successfully");
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseMessageKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "delete-account-by-account-name", groupId = "account-component",
            containerFactory = "stringKafkaListenerFactory")
    public void deleteAccountByAccountName(String accountName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: delete-account-by-account-name with correlation id: {} ", correlationId);
        logger.info("Trying to find Account with Name: {}", accountName);
        Account account = accountRepository.findByAccountName(accountName)
                .map(AccountEntity -> {
                    AccountEntity.setStatus("PRE-REMOVED");
                    logger.debug("Account Status was changed to - PRE-REMOVED: {}", AccountEntity);
                    return accountRepository.save(AccountEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Account with such Name: {} was not found", accountName);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such Account Name: " + accountName + " was not found correlationId:" + correlationId);
                });

        logger.info("Account was found, Trying to find All Account Cards with Account Name: {}", accountName);
        cardComponentClient.deleteAllAccountCardsByAccountId(account.getId());
        logger.debug("All found account Cards Status was changed to - DEACTIVATED");

        logger.info("Trying to create topic: delete-account-by-account-name-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "delete-account-by-account-name-response", null, "Account deleted successfully");
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseMessageKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "delete-account-by-account-id", groupId = "account-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void deleteAllUserAccountsByUserId(UUID userId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: delete-account-by-account-id with correlation id: {} ", correlationId);
        logger.info("Trying to find User with User ID: {}", userId);
        UsersDTO userDTO = usersComponentClient.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User with such ID was not found: {}", userId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });

        logger.info("User was found successfully: {}, \nTrying to find All User Accounts", userDTO);
        accountRepository.findByAccountHolderFullName(userDTO.getFullName())
                .forEach(AccountEntity -> {
                    AccountEntity.setStatus("PRE-REMOVED");
                    logger.debug("Account was found and status it was changed to - PRE-REMOVED: {}", AccountEntity);
                    logger.info("Trying to find All Account Cards with Account ID: {}", AccountEntity.getId());
                    cardComponentClient.deleteAllAccountCardsByAccountId(AccountEntity.getId());
                    logger.debug("All found account Cards Status was changed to - DEACTIVATED");
                    accountRepository.save(AccountEntity);
                });

        logger.info("Trying to create topic: delete-account-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "delete-account-by-account-id-response", null, "Accounts deleted successfully");
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseMessageKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }
}