package com.example.cardcomponent.service;

import com.example.cardcomponent.dto.AccountDTO;
import com.example.cardcomponent.dto.CardDTO;
import com.example.cardcomponent.exception.CustomKafkaException;
import com.example.cardcomponent.feign.AccountComponentClient;
import com.example.cardcomponent.feign.UsersComponentClient;
import com.example.cardcomponent.model.Card;
import com.example.cardcomponent.repository.CardRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class KafkaCardServiceImpl implements KafkaCardService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaCardServiceImpl.class);
    private static final String ALLOCATED_TOPIC_LOG = "Topic was created and allocated in kafka broker successfully: {}";
    private static final String CARD_SEARCHING_LOG = "Trying to find Card by: {}";
    private static final String CARD_NOT_FOUND_LOG = "Card was not found by: {}";
    private static final String ACCOUNT_SEARCHING_LOG = "Trying to find Account by: {}";
    private static final String ACCOUNT_NOT_FOUND_LOG = "Account was not found by: {}";
    private static final String USER_SEARCHING_LOG = "Trying to find User by: {}";
    private static final String USER_NOT_FOUND_LOG = "User was not found by: {}";
    private static final String FOUND_CARDS_LOG = "Cards was found in DB: {}";
    
    private final KafkaTemplate<String, CardDTO> responseDTOKafkaTemplate;
    private final KafkaTemplate<String, List<CardDTO>> responseListOfDTOSKafkaTemplate;
    private final KafkaTemplate<String, String> responseMessageKafkaTemplate;

    private final CardRepository cardRepository;
    private final UsersComponentClient usersComponentClient;
    private final AccountComponentClient accountComponentClient;
    private final Random random = new Random();

    public KafkaCardServiceImpl(CardRepository cardRepository,
                                @Qualifier("Users-Components") UsersComponentClient usersComponentClient,
                                @Qualifier("Account-Components") AccountComponentClient accountComponentClient, KafkaTemplate<String, CardDTO> responseDTOKafkaTemplate, KafkaTemplate<String, List<CardDTO>> responseListOfDTOSKafkaTemplate, KafkaTemplate<String, String> responseMessageKafkaTemplate) {
        this.cardRepository = cardRepository;
        this.usersComponentClient = usersComponentClient;
        this.accountComponentClient = accountComponentClient;
        this.responseDTOKafkaTemplate = responseDTOKafkaTemplate;
        this.responseListOfDTOSKafkaTemplate = responseListOfDTOSKafkaTemplate;
        this.responseMessageKafkaTemplate = responseMessageKafkaTemplate;
    }

    private CardDTO convertCardModelToDTO(Card card) {
        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(card.getId());
        cardDTO.setCardNumber(card.getCardNumber());
        cardDTO.setCardHolderFullName(card.getCardHolderFullName());
        cardDTO.setCardHolderId(card.getCardHolderUUID());
        cardDTO.setExpirationDate(card.getExpirationDate());
        cardDTO.setCvv(card.getCvv());
        cardDTO.setStatus(card.getStatus());
        return cardDTO;
    }

    private Card cardGenerator(AccountDTO accountDTO) {
        Card card = new Card();
        card.setCardHolderUUID(accountDTO.getId());
        LOGGER.debug("Generating card number");
        card.setCardNumber(cardNumberGenerator());
        card.setCardHolderFullName(accountDTO.getAccountHolderFullName());
        card.setExpirationDate(LocalDate.now().plusYears(random.nextInt(5) + 1));
        card.setCvv(String.format("%03d", random.nextInt(1000)));
        card.setStatus("ACTIVE");
        return card;
    }

    private String cardNumberGenerator() {
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            cardNumber.append(String.format("%04d", random.nextInt(10000)));
            if (i < 3) {
                cardNumber.append(" ");
            }
        }
        LOGGER.debug("Card number generated successfully: {}", cardNumber);
        return cardNumber.toString();
    }

    @Override
    @KafkaListener(topics = "create-card-by-account-id", groupId = "card-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void createCard(UUID accountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: create-card-by-account-id with correlation id: {} ", correlationId);
        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountId);
        AccountDTO accountDTO = accountComponentClient.findById(accountId)
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountId);
                    throw new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found");
                });

        LOGGER.debug("Generating Card");
        Card card = cardRepository.save(cardGenerator(accountDTO));
        LOGGER.info("Card generated successfully: {}", card);
        convertCardModelToDTO(card);

        LOGGER.info("Trying to create topic: create-card-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, CardDTO> responseTopic = new ProducerRecord<>(
                "create-card-by-account-id-response", null, convertCardModelToDTO(card));
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-card-by-id", groupId = "card-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getCardById(UUID cardId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-card-by-id with correlation id: {} ", correlationId);
        LOGGER.info(CARD_SEARCHING_LOG, cardId);
        CardDTO cardDTO = cardRepository.findById(cardId)
                .map(cardEntity -> {
                    LOGGER.info("Card was found and received to the Controller: {}", cardEntity);
                    return convertCardModelToDTO(cardEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(CARD_NOT_FOUND_LOG, cardId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Card with such ID: " + cardId + " was not found");
                });

        LOGGER.info("Trying to create topic: get-card-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, CardDTO> responseTopic = new ProducerRecord<>(
                "get-card-by-id-response", null, cardDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-card-by-card-number", groupId = "card-component",
            containerFactory = "stringKafkaListenerFactory")
    public void getCardByCardNumber(String cardNumber, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-card-by-card-number with correlation id: {} ", correlationId);
        LOGGER.info(CARD_SEARCHING_LOG, cardNumber);
        CardDTO cardDTO = cardRepository.findByCardNumber(cardNumber)
                .map(cardEntity -> {
                    LOGGER.info("Card was found in DB: {}", cardEntity);
                    return convertCardModelToDTO(cardEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(CARD_NOT_FOUND_LOG, cardNumber);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Card with such Card Number: " + cardNumber + " was not found");
                });

        LOGGER.info("Trying to create topic: get-card-by-card-number-response with correlation id: {} ", correlationId);
        ProducerRecord<String, CardDTO> responseTopic = new ProducerRecord<>(
                "get-card-by-card-number-response", null, cardDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-cards-by-holder-name", groupId = "card-component",
            containerFactory = "stringKafkaListenerFactory")
    public void getCardsByCardHolderFullName(String cardHolderFullName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-all-cards-by-holder-name with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, cardHolderFullName);
        usersComponentClient.findByFullName(cardHolderFullName)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, cardHolderFullName);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such Full Name: " + cardHolderFullName + " was not found");
                });

        LOGGER.info("Trying to find all Cards linked to User Name: {}", cardHolderFullName);
        List<CardDTO> cardDTOS = cardRepository.findAllByCardHolderFullName(cardHolderFullName).stream()
                .map(this::convertCardModelToDTO)
                .toList();
        LOGGER.info(FOUND_CARDS_LOG, cardDTOS);

        LOGGER.info("Trying to create topic: get-all-cards-by-holder-name-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<CardDTO>> responseTopic = new ProducerRecord<>(
                "get-all-cards-by-holder-name-response", null, cardDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseListOfDTOSKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-cards-by-account-id", groupId = "card-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getAllAccountCardsByAccountId(UUID accountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-all-cards-by-account-id with correlation id: {} ", correlationId);
        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountId);
        accountComponentClient.findById(accountId)
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found");
                });

        LOGGER.info("Trying to find all Cards linked to Account with ID: {}", accountId);
        List<CardDTO> cardDTOS = cardRepository.findAllByAccountId(accountId).stream()
                .map(this::convertCardModelToDTO)
                .toList();
        LOGGER.info(FOUND_CARDS_LOG, cardDTOS);

        LOGGER.info("Trying to create topic: get-all-cards-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<CardDTO>> responseTopic = new ProducerRecord<>(
                "get-all-cards-by-account-id-response", null, cardDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseListOfDTOSKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-cards-by-holder-id", groupId = "card-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getAllUserCardsByCardHolderId(UUID holderId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-all-cards-by-holder-id with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, holderId);
        usersComponentClient.findById(holderId)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, holderId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + holderId + " was not found");
                });

        LOGGER.info("Trying to find all Cards linked to User with ID: {}", holderId);
        List<CardDTO> cardDTOS = cardRepository.findAllByCardHolderUUID(holderId).stream()
                .map(this::convertCardModelToDTO)
                .toList();
        LOGGER.info(FOUND_CARDS_LOG, cardDTOS);

        LOGGER.info("Trying to create topic: get-all-cards-by-holder-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<CardDTO>> responseTopic = new ProducerRecord<>(
                "get-all-cards-by-holder-id-response", null, cardDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseListOfDTOSKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "update-card-status-by-id", groupId = "card-component",
            containerFactory = "mapUUIDToStringKafkaListenerFactory")
    public void getAllUserCardsByStatus(UUID holderId, String status, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: update-card-status-by-id with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, holderId);
        usersComponentClient.findById(holderId)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, holderId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + holderId + " was not found");
                });

        LOGGER.info("Trying to find all Cards linked to User by" +
                " Card Status: {}", status);
        List<CardDTO> cardDTOS = cardRepository.findAllByCardHolderUUIDAndStatus(holderId, status).stream()
                .map(this::convertCardModelToDTO)
                .toList();
        LOGGER.info(FOUND_CARDS_LOG, cardDTOS);

        LOGGER.info("Trying to create topic: update-card-status-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<CardDTO>> responseTopic = new ProducerRecord<>(
                "update-card-status-by-id-response", null, cardDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseListOfDTOSKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-expired-cards-by-holder-id", groupId = "card-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getAllExpiredCards(UUID holderId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-all-expired-cards-by-holder-id with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, holderId);
        usersComponentClient.findById(holderId)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, holderId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + holderId + " was not found");
                });

        LOGGER.info("Trying to find all expired Cards linked to User with ID: {}", holderId);
        List<CardDTO> cardDTOS = cardRepository.findAllByCardHolderUUID(holderId).stream()
                .filter(card -> card.getExpirationDate().isBefore(LocalDate.now()))
                .map(filteredEntity -> {
                    LOGGER.info(FOUND_CARDS_LOG, filteredEntity);
                    return convertCardModelToDTO(filteredEntity);
                })
                .toList();

        LOGGER.info("Trying to create topic: get-all-expired-cards-by-holder-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<CardDTO>> responseTopic = new ProducerRecord<>(
                "get-all-expired-cards-by-holder-id-response", null, cardDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseListOfDTOSKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-active-cards-by-holder-id", groupId = "card-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getAllActiveCards(UUID holderId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-all-active-cards-by-holder-id with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, holderId);
        usersComponentClient.findById(holderId)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, holderId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + holderId + " was not found");
                });
        LOGGER.info("Trying to find all active Cards linked to User with ID: {}", holderId);
        List<CardDTO> cardDTOS = cardRepository.findAllByCardHolderUUID(holderId).stream()
                .filter(card -> card.getExpirationDate().isAfter(LocalDate.now()))
                .map(filteredEntity -> {
                    LOGGER.info(FOUND_CARDS_LOG, filteredEntity);
                    return convertCardModelToDTO(filteredEntity);
                })
                .toList();

        LOGGER.info("Trying to create topic: get-all-active-cards-by-holder-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<CardDTO>> responseTopic = new ProducerRecord<>(
                "get-all-active-cards-by-holder-id-response", null, cardDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseListOfDTOSKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "update-card-status-by-id", groupId = "card-component",
            containerFactory = "mapUUIDToStringKafkaListenerFactory")
    public void updateCardStatusById(UUID cardId, String status, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: update-card-status-by-id with correlation id: {} ", correlationId);
        LOGGER.info(CARD_SEARCHING_LOG, cardId);
        CardDTO cardDTO = cardRepository.findById(cardId)
                .map(cardEntity -> {
                    cardEntity.setStatus(status);
                    cardRepository.save(cardEntity);
                    LOGGER.info("Card status was updated and received to the Controller: {}", cardEntity);
                    return convertCardModelToDTO(cardEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(CARD_NOT_FOUND_LOG, cardId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Card with such ID: " + cardId + " was not found");
                });

        LOGGER.info("Trying to create topic: update-card-status-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, CardDTO> responseTopic = new ProducerRecord<>(
                "update-card-status-by-id-response", null, cardDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "update-card-status-by-card-number", groupId = "card-component",
            containerFactory = "mapStringToStringKafkaListenerFactory")
    public void updateCardStatusByCardNumber(String cardNumber, String status, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: update-card-status-by-card-number with correlation id: {} ", correlationId);
        LOGGER.info(CARD_SEARCHING_LOG, cardNumber);
        CardDTO cardDTO = cardRepository.findByCardNumber(cardNumber)
                .map(cardEntity -> {
                    cardEntity.setStatus(status);
                    cardRepository.save(cardEntity);
                    LOGGER.info("Card status was updated and received to the Controller: {}", cardEntity);
                    return convertCardModelToDTO(cardEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(CARD_NOT_FOUND_LOG, cardNumber);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Card with such Card Number: " + cardNumber + " was not found");
                });

        LOGGER.info("Trying to create topic: update-card-status-by-card-number-response with correlation id: {} ", correlationId);
        ProducerRecord<String, CardDTO> responseTopic = new ProducerRecord<>(
                "update-card-status-by-card-number-response", null, cardDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "delete-card-by-id", groupId = "card-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void deleteCardById(UUID cardId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: delete-card-by-id with correlation id: {} ", correlationId);
        LOGGER.info(CARD_SEARCHING_LOG, cardId);
        cardRepository.findById(cardId)
                .map(cardEntity -> {
                    cardEntity.setStatus("DEACTIVATED");
                    LOGGER.info("Cards Status was changed to - DEACTIVATED: {}", cardEntity);
                    return cardRepository.save(cardEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(CARD_NOT_FOUND_LOG, cardId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Card with such ID: " + cardId + " was not found");
                });

        LOGGER.info("Trying to create topic: delete-card-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "delete-card-by-id-response", null, "Card deleted successfully");
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseMessageKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "delete-card-by-account-id", groupId = "card-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void deleteAllAccountCardsByAccountId(UUID accountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: delete-card-by-account-id with correlation id: {} ", correlationId);
        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountId);
        accountComponentClient.findById(accountId)
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found");
                });

        LOGGER.info("Trying to find Account Cards with Account ID: {}", accountId);
        cardRepository.findAllByAccountId(accountId)
                .forEach(cardEntity -> {
                    cardEntity.setStatus("DEACTIVATED");
                    LOGGER.info("Account Cards Status was changed to - DEACTIVATED: {}", cardEntity);
                });

        LOGGER.info("Trying to create topic: delete-card-by-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "delete-card-by-account-id-response", null, "Cards deleted successfully");
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseMessageKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "delete-card-by-holder-id", groupId = "card-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void deleteAllUsersCardsByCardHolderUUID(UUID cardHolderUUID, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: delete-card-by-holder-id with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, cardHolderUUID);
        usersComponentClient.findById(cardHolderUUID)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, cardHolderUUID);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + cardHolderUUID + " was not found");
                });

        LOGGER.info("Trying to find User Cards with User ID: {}", cardHolderUUID);
        cardRepository.findAllByCardHolderUUID(cardHolderUUID)
                .forEach(cardEntity -> {
                    cardEntity.setStatus("DEACTIVATED");
                    LOGGER.info("Account Cards Status was changed to - DEACTIVATED: {}", cardEntity);
                });

        LOGGER.info("Trying to create topic: delete-card-by-holder-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "delete-card-by-holder-id-response", null, "Cards deleted successfully");
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseMessageKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }
}
