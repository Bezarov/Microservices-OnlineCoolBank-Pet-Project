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
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAccountServiceImpl.class);
    private static final String ALLOCATED_TOPIC_LOG = "Topic was created and allocated in kafka broker successfully: {}";
    private static final String ACCOUNT_SEARCHING_LOG = "Trying to find Account by: {}";
    private static final String ACCOUNT_NOT_FOUND_LOG = "Account was not found by: {}";
    private static final String USER_SEARCHING_LOG = "Trying to find User by: {}";
    private static final String USER_NOT_FOUND_LOG = "User was not found by: {}";

    private final KafkaTemplate<String, AccountDTO> responseDTOKafkaTemplate;
    private final KafkaTemplate<String, List<AccountDTO>> responseDTOSKafkaTemplate;
    private final KafkaTemplate<String, String> responseMessageKafkaTemplate;
    private final KafkaTemplate<String, BigDecimal> responseBigDecimalKafkaTemplate;

    private final AccountRepository accountRepository;
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
        LOGGER.info("Got request from kafka topic: create-account-by-user-id with correlation id: {} ", correlationId);
        ObjectMapper objectMapper = new ObjectMapper();
        String userId = userIdToAccountDTOMap.keySet().iterator().next().replaceAll("\"", "");
        AccountDTO accountDTO = objectMapper.convertValue(userIdToAccountDTOMap.get(userId), AccountDTO.class);

        LOGGER.info(USER_SEARCHING_LOG, userId);
        UsersDTO userDTO = usersComponentClient.findById(UUID.fromString(userId))
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });
        LOGGER.info("User was found successfully: {}", userId);
        LOGGER.info("Trying to create Account: {}", accountDTO);
        accountRepository.findByAccountName(accountDTO.getAccountName())
                .ifPresent(accountEntity -> {
                    LOGGER.error("Account with such name already exists: {}", accountEntity);
                    throw new CustomKafkaException(HttpStatus.FOUND, "Account with such name: " +
                            accountDTO.getAccountName() + " already exists correlationId:" + correlationId);
                });
        Account account = accountRepository.save(convertAccountDTOToModel(userDTO, accountDTO));
        LOGGER.debug("Account created successfully: {}", accountDTO);

        LOGGER.info("Trying to create topic: create-account-by-user-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "create-card-by-account-id-response", null, convertAccountModelToDTO(account));
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-account-by-account-name", groupId = "account-component",
            containerFactory = "stringKafkaListenerFactory")
    public void getAccountByAccountName(String accountName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-account-by-account-name with correlation id: {} ", correlationId);
        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountName);
        AccountDTO accountDTO = accountRepository.findByAccountName(accountName)
                .map(accountEntity -> {
                    LOGGER.debug("Account was found in DB: {}", accountEntity);
                    return convertAccountModelToDTO(accountEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountName);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such name: " + accountName + " was not found correlationId:" + correlationId);
                });
        LOGGER.info("Trying to find account cards by accountId: {}", accountDTO.getId());
        List<CardDTO> cardDTOS = cardComponentClient.findAllCardsByAccountId(accountDTO.getId())
                .stream()
                .peek(cardDTO -> LOGGER.info("Card was found and added to AccountDTO response: {}", cardDTO))
                .toList();
        accountDTO.setCards(cardDTOS);

        LOGGER.info("Trying to create topic: get-account-by-account-name-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "get-account-by-account-name-response", null, accountDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-account-by-account-id", groupId = "account-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getAccountById(UUID accountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-account-by-account-id with correlation id: {} ", correlationId);
        LOGGER.info(ALLOCATED_TOPIC_LOG, accountId);
        AccountDTO accountDTO = accountRepository.findById(accountId)
                .map(accountEntity -> {
                    LOGGER.debug("Account was found in DB: {}", accountEntity);
                    return convertAccountModelToDTO(accountEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found correlationId:" + correlationId);
                });
        LOGGER.info("Trying to find account cards by accountId: {}", accountDTO.getId());
        List<CardDTO> cardDTOS = cardComponentClient.findAllCardsByAccountId(accountDTO.getId())
                .stream()
                .peek(cardDTO -> LOGGER.info("Card was found and added to AccountDTO response: {}", cardDTO))
                .toList();
        accountDTO.setCards(cardDTOS);
        LOGGER.info("Trying to create topic: get-account-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "get-account-by-account-id-response", null, accountDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-accounts-by-user-id", groupId = "account-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getAllUserAccountsByUserId(UUID userId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-all-accounts-by-user-id with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, userId);
        UsersDTO usersDTO = usersComponentClient.findById(userId)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });
        LOGGER.info("User was found successfully: {} \nTrying to find All Accounts linked to user with ID: {}",
                usersDTO, userId);

        List<AccountDTO> accountDTOS = accountRepository.findByAccountHolderFullName(usersDTO.getFullName())
                .stream()
                .map(this::convertAccountModelToDTO)
                .toList();
        LOGGER.debug("Accounts was found: {} \nTrying to find it cards", accountDTOS);
        accountDTOS.forEach(accountDTO -> {
            LOGGER.debug("Trying to find account by id: {}, cards", accountDTO.getId());
            cardComponentClient.findAllCardsByAccountId(accountDTO.getId());
        });

        LOGGER.info("Trying to create topic: get-all-accounts-by-user-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<AccountDTO>> responseTopic = new ProducerRecord<>(
                "get-account-by-account-id-response", null, accountDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-accounts-by-holder-full-name", groupId = "account-component",
            containerFactory = "stringKafkaListenerFactory")
    public void getAllAccountsByHolderFullName(String accountHolderFullName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-all-accounts-by-holder-full-name with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, accountHolderFullName);
        UsersDTO usersDTO = usersComponentClient.findByFullName(accountHolderFullName)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, accountHolderFullName);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such full-name: " + accountHolderFullName + " was not found correlationId:" + correlationId);
                });
        LOGGER.info("User was found successfully: {} \nTrying to find All Accounts linked to user with ID: {}",
                usersDTO, usersDTO.getId());

        List<AccountDTO> accountDTOS = accountRepository.findByAccountHolderFullName(usersDTO.getFullName())
                .stream()
                .map(this::convertAccountModelToDTO)
                .toList();
        LOGGER.debug("Accounts was found: {} \nTrying to find it cards", accountDTOS);
        accountDTOS.forEach(accountDTO -> {
            LOGGER.debug("Trying to find account by id: {}, cards", accountDTO.getId());
            cardComponentClient.findAllCardsByAccountId(accountDTO.getId());
        });

        LOGGER.info("Trying to create topic: get-all-accounts-by-holder-full-name-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<AccountDTO>> responseTopic = new ProducerRecord<>(
                "get-all-accounts-by-holder-full-name-response", null, accountDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-balance-by-account-id", groupId = "account-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getBalanceByAccountId(UUID accountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-balance-by-account-id with correlation id: {} ", correlationId);
        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountId);
        BigDecimal balance = accountRepository.findById(accountId)
                .map(accountEntity -> {
                    LOGGER.debug("Account was found and it balance successfully: {}", accountEntity.getBalance());
                    return accountEntity.getBalance();
                })
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found correlationId:" + correlationId);
                });

        LOGGER.info("Trying to create topic: get-balance-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, BigDecimal> responseTopic = new ProducerRecord<>(
                "get-balance-by-account-id-response", null, balance);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseBigDecimalKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-accounts-by-status", groupId = "account-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void getAllAccountsWithStatusByUserId(Map<String, String> userIdToAccountStatusMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-all-accounts-by-status with correlation id: {} ", correlationId);
        String userId = userIdToAccountStatusMap.keySet().iterator().next().replaceAll("\"", "");
        String accountStatus = userIdToAccountStatusMap.get(userId);

        LOGGER.info(USER_SEARCHING_LOG, userId);
        UsersDTO usersDTO = usersComponentClient.findById(UUID.fromString(userId))
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });
        LOGGER.info("User was found successfully: {}", usersDTO);

        LOGGER.info("Trying to find All Accounts linked to user with ID: {}, with status: {}", userId, accountStatus);
        List<AccountDTO> accountDTOS = accountRepository.findByAccountHolderFullName(usersDTO.getFullName())
                .stream()
                .filter(account -> account.getStatus().equals(accountStatus))
                .map(this::convertAccountModelToDTO)
                .toList();
        LOGGER.info("Found Accounts with status: {}, in quantity: {} ", accountStatus, accountDTOS.size());
        LOGGER.info("Trying to create topic: get-all-accounts-by-status-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<AccountDTO>> responseTopic = new ProducerRecord<>(
                "get-all-accounts-by-status-response", null, accountDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "refill-account-by-account-id", groupId = "account-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void refillAccount(Map<String, BigDecimal> accountIdToAmountMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: refill-account-by-account-id with correlation id: {} ", correlationId);
        String accountId = accountIdToAmountMap.keySet().iterator().next().replaceAll("\"", "");
        BigDecimal amount = accountIdToAmountMap.get(accountId);

        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountId);
        AccountDTO accountDTO = accountRepository.findById(UUID.fromString(accountId))
                .map(accountEntity -> {
                    accountEntity.setBalance(accountEntity.getBalance().add(amount));
                    accountRepository.save(accountEntity);
                    LOGGER.debug("Account was found and balance was refilled successfully: {}", accountEntity.getBalance());
                    return convertAccountModelToDTO(accountEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found correlationId:" + correlationId);
                });

        LOGGER.info("Trying to create topic: refill-account-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "refill-account-by-account-id-response", null, accountDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "update-account-by-account-id", groupId = "account-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void updateAccountById(Map<String, AccountDTO> accountIdToAccountDTOMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: update-account-by-account-id with correlation id: {} ", correlationId);
        String accountId = accountIdToAccountDTOMap.keySet().iterator().next().replaceAll("\"", "");
        AccountDTO accountDTO = accountIdToAccountDTOMap.get(accountId);

        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountId);
        AccountDTO responseAccountDTO = accountRepository.findById(UUID.fromString(accountId))
                .map(accountEntity -> {
                    accountEntity.setAccountName(accountDTO.getAccountName());
                    accountEntity.setStatus(accountDTO.getStatus());
                    accountEntity.setAccountType(accountDTO.getAccountType());
                    accountEntity.setCurrency(accountDTO.getCurrency());
                    accountRepository.save(accountEntity);
                    LOGGER.debug("Account updated successfully: {}", accountEntity);
                    return convertAccountModelToDTO(accountEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found correlationId:" + correlationId);
                });

        LOGGER.info("Trying to create topic: update-account-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "update-account-by-account-id-response", null, responseAccountDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "update-account-status-by-account-id", groupId = "account-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void updateAccountStatusById(Map<String, String> accountIdToStatusMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: update-account-status-by-account-id with correlation id: {} ", correlationId);
        String accountId = accountIdToStatusMap.keySet().iterator().next().replaceAll("\"", "");
        String accountStatus = accountIdToStatusMap.get(accountId);

        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountId);
        AccountDTO accountDTO = accountRepository.findById(UUID.fromString(accountId))
                .map(accountEntity -> {
                    accountEntity.setStatus(accountStatus);
                    accountRepository.save(accountEntity);
                    LOGGER.debug("Account Status updated successfully: {}", accountEntity);
                    return convertAccountModelToDTO(accountEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found correlationId:" + correlationId);
                });

        LOGGER.info("Trying to create topic: update-account-status-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "get-account-by-account-name-response", null, accountDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "update-account-status-by-account-id", groupId = "account-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void updateAccountBalanceById(Map<String, BigDecimal> accountIdToNewBalanceMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: update-account-status-by-account-id with correlation id: {} ", correlationId);
        String accountId = accountIdToNewBalanceMap.keySet().iterator().next().replaceAll("\"", "");
        BigDecimal newBalance = accountIdToNewBalanceMap.get(accountId);

        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountId);
        AccountDTO accountDTO = accountRepository.findById(UUID.fromString(accountId))
                .map(accountEntity -> {
                    accountEntity.setBalance(newBalance);
                    accountRepository.save(accountEntity);
                    LOGGER.debug("Account Balance updated successfully: {}", accountEntity);
                    return convertAccountModelToDTO(accountEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found correlationId:" + correlationId);
                });

        LOGGER.info("Trying to create topic: update-account-status-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "update-account-status-by-account-id-response", null, accountDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "update-account-balance-by-account-id", groupId = "account-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void updateAccountBalanceByAccountName(Map<String, BigDecimal> accountNameToNewBalanceMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: update-account-balance-by-account-id with correlation id: {} ", correlationId);
        String accountName = accountNameToNewBalanceMap.keySet().iterator().next().replaceAll("\"", "");
        BigDecimal newBalance = accountNameToNewBalanceMap.get(accountName);

        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountName);
        AccountDTO accountDTO = accountRepository.findByAccountName(accountName)
                .map(accountEntity -> {
                    accountEntity.setBalance(newBalance);
                    accountRepository.save(accountEntity);
                    LOGGER.info("Account balance with name: {}, updated successfully: {}", accountName, accountEntity);
                    return convertAccountModelToDTO(accountEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountName);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such Name: " + accountName + " was not found correlationId:" + correlationId);
                });

        LOGGER.info("Trying to create topic: update-account-balance-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(
                "update-account-balance-by-account-id-response", null, accountDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "delete-account-by-account-id", groupId = "account-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void deleteAccountByAccountId(UUID accountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: delete-account-by-account-id with correlation id: {} ", correlationId);
        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountId);
        accountRepository.findById(accountId)
                .map(accountEntity -> {
                    accountEntity.setStatus("PRE-REMOVED");
                    LOGGER.debug("Account Status was changed to - PRE-REMOVED: {}", accountEntity);
                    return accountRepository.save(accountEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found correlationId:" + correlationId);
                });
        LOGGER.info("Account was found, Trying to find All Account Cards with Account ID: {}", accountId);

        cardComponentClient.deleteAllAccountCardsByAccountId(accountId);
        LOGGER.debug("All found account Cards Status was changed to - DEACTIVATED");

        LOGGER.info("Trying to create topic: delete-account-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "delete-account-by-account-id-response", null, "Account deleted successfully");
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseMessageKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "delete-account-by-account-name", groupId = "account-component",
            containerFactory = "stringKafkaListenerFactory")
    public void deleteAccountByAccountName(String accountName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: delete-account-by-account-name with correlation id: {} ", correlationId);
        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountName);
        Account account = accountRepository.findByAccountName(accountName)
                .map(accountEntity -> {
                    accountEntity.setStatus("PRE-REMOVED");
                    LOGGER.debug("Account Status was changed to - PRE-REMOVED: {}", accountEntity);
                    return accountRepository.save(accountEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountName);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such Account Name: " + accountName + " was not found correlationId:" + correlationId);
                });

        LOGGER.info("Account was found, Trying to find All Account Cards with Account Name: {}", accountName);
        cardComponentClient.deleteAllAccountCardsByAccountId(account.getId());
        LOGGER.debug("All found account Cards Status was changed to - DEACTIVATED");

        LOGGER.info("Trying to create topic: delete-account-by-account-name-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "delete-account-by-account-name-response", null, "Account deleted successfully");
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseMessageKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "delete-account-by-account-id", groupId = "account-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void deleteAllUserAccountsByUserId(UUID userId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: delete-account-by-account-id with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, userId);
        UsersDTO userDTO = usersComponentClient.findById(userId)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });

        LOGGER.info("User was found successfully: {}, \nTrying to find All User Accounts", userDTO);
        accountRepository.findByAccountHolderFullName(userDTO.getFullName())
                .forEach(accountEntity -> {
                    accountEntity.setStatus("PRE-REMOVED");
                    LOGGER.debug("Account was found and status it was changed to - PRE-REMOVED: {}", accountEntity);
                    LOGGER.info("Trying to find All Account Cards with Account ID: {}", accountEntity.getId());
                    cardComponentClient.deleteAllAccountCardsByAccountId(accountEntity.getId());
                    LOGGER.debug("All found account Cards Status was changed to - DEACTIVATED");
                    accountRepository.save(accountEntity);
                });

        LOGGER.info("Trying to create topic: delete-account-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "delete-account-by-account-id-response", null, "Accounts deleted successfully");
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseMessageKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }
}