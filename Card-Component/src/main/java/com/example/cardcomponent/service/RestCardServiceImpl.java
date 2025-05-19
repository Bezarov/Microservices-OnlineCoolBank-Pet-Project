package com.example.cardcomponent.service;

import com.example.cardcomponent.dto.CardDTO;
import com.example.cardcomponent.feign.AccountComponentClient;
import com.example.cardcomponent.feign.UsersComponentClient;
import com.example.cardcomponent.model.Card;
import com.example.cardcomponent.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class RestCardServiceImpl implements RestCardService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestCardServiceImpl.class);
    private static final String ACCOUNT_SEARCHING_LOG = "Trying to find Account by: {}";
    private static final String ACCOUNT_NOT_FOUND_LOG = "Account was not found by: {}";
    private static final String ACCOUNT_WAS_FOUND_LOG = "Account with: {} was found";
    private static final String USER_SEARCHING_LOG = "Trying to find User by: {}";
    private static final String USER_NOT_FOUND_LOG = "User was not found by: {}";
    private static final String USER_WAS_FOUND_LOG = "User with: {} was found";
    private static final String CARD_SEARCHING_LOG = "Trying to find Card by: {}";
    private static final String CARD_FOUND_LOG = "Card was found and received to the Controller: {}";

    private final CardRepository cardRepository;
    private final UsersComponentClient usersComponentClient;
    private final AccountComponentClient accountComponentClient;

    public RestCardServiceImpl(CardRepository cardRepository,
                               @Qualifier("Users-Components") UsersComponentClient usersComponentClient,
                               @Qualifier("Account-Components") AccountComponentClient accountComponentClient) {
        this.cardRepository = cardRepository;
        this.usersComponentClient = usersComponentClient;
        this.accountComponentClient = accountComponentClient;
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


    @Override
    public CardDTO getCardById(UUID cardId) {
        LOGGER.info(CARD_SEARCHING_LOG, cardId);
        return cardRepository.findById(cardId)
                .map(cardEntity -> {
                    LOGGER.info(CARD_FOUND_LOG, cardEntity);
                    return convertCardModelToDTO(cardEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(CARD_FOUND_LOG, cardId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Card with such ID: " + cardId + " was not found");
                });
    }

    @Override
    public CardDTO getCardByCardNumber(String cardNumber) {
        LOGGER.info(CARD_SEARCHING_LOG, cardNumber);
        return cardRepository.findByCardNumber(cardNumber)
                .map(cardEntity -> {
                    LOGGER.info(CARD_FOUND_LOG, cardEntity);
                    return convertCardModelToDTO(cardEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(CARD_FOUND_LOG, cardNumber);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Card with such Card Number: " + cardNumber + " was not found");
                });
    }

    @Override
    public List<CardDTO> getCardsByCardHolderFullName(String cardHolderFullName) {
        LOGGER.info(USER_SEARCHING_LOG, cardHolderFullName);
        usersComponentClient.existenceCheck(cardHolderFullName)
                .filter(Boolean.TRUE::equals)
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, cardHolderFullName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such Full Name: " + cardHolderFullName + " was not found");
                });
        LOGGER.debug(USER_WAS_FOUND_LOG, cardHolderFullName);

        LOGGER.info("Trying to find all Cards linked to User Name: {}", cardHolderFullName);
        List<Card> cards = cardRepository.findAllByCardHolderFullName(cardHolderFullName);
        LOGGER.info(CARD_FOUND_LOG, cards);
        return cards.stream()
                .map(this::convertCardModelToDTO)
                .toList();
    }

    @Override
    public List<CardDTO> getAllAccountCardsByAccountId(UUID accountId) {
        LOGGER.info(ACCOUNT_SEARCHING_LOG, accountId);
        accountComponentClient.existenceCheck(accountId)
                .filter(Boolean.TRUE::equals)
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_NOT_FOUND_LOG, accountId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found");
                });
        LOGGER.debug(ACCOUNT_WAS_FOUND_LOG, accountId);

        LOGGER.info("Trying to find all Cards linked to Account with ID: {}", accountId);
        List<Card> cards = cardRepository.findAllByAccountId(accountId);
        LOGGER.info(CARD_FOUND_LOG, cards);
        return cards.stream()
                .map(this::convertCardModelToDTO)
                .toList();
    }

    @Override
    public List<CardDTO> getAllUserCardsByCardHolderId(UUID holderId) {
        LOGGER.info(USER_SEARCHING_LOG, holderId);
        usersComponentClient.existenceCheck(holderId)
                .filter(Boolean.TRUE::equals)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, holderId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + holderId + " was not found");
                });
        LOGGER.debug(USER_WAS_FOUND_LOG, holderId);

        LOGGER.info("Trying to find all Cards linked to User with ID: {}", holderId);
        List<Card> cards = cardRepository.findAllByCardHolderUUID(holderId);
        LOGGER.info(CARD_FOUND_LOG, cards);
        return cards.stream()
                .map(this::convertCardModelToDTO)
                .toList();
    }

    @Override
    public List<CardDTO> getAllUserCardsByStatus(UUID holderId, String status) {
        LOGGER.info(USER_SEARCHING_LOG, holderId);
        usersComponentClient.existenceCheck(holderId)
                .filter(Boolean.TRUE::equals)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, holderId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + holderId + " was not found");
                });
        LOGGER.debug(USER_WAS_FOUND_LOG, holderId);

        LOGGER.info("Trying to find all Cards linked to User by" +
                " Card Status: {}", status);
        List<Card> cards = cardRepository.findAllByCardHolderUUIDAndStatus(holderId, status);
        LOGGER.info(CARD_FOUND_LOG, cards);
        return cards.stream()
                .map(this::convertCardModelToDTO)
                .toList();
    }

    @Override
    public List<CardDTO> getAllExpiredCards(UUID holderId) {
        LOGGER.info(USER_SEARCHING_LOG, holderId);
        usersComponentClient.existenceCheck(holderId)
                .filter(Boolean.TRUE::equals)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, holderId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + holderId + " was not found");
                });
        LOGGER.debug(USER_WAS_FOUND_LOG, holderId);

        LOGGER.info("Trying to find all expired Cards linked to User with ID: {}", holderId);
        List<Card> cards = cardRepository.findAllByCardHolderUUID(holderId);
        return cards.stream()
                .filter(card -> card.getExpirationDate().isBefore(LocalDate.now()))
                .map(filteredEntity -> {
                    LOGGER.info(CARD_FOUND_LOG, filteredEntity);
                    return convertCardModelToDTO(filteredEntity);
                })
                .toList();
    }

    @Override
    public List<CardDTO> getAllActiveCards(UUID holderId) {
        LOGGER.info(USER_SEARCHING_LOG, holderId);
        usersComponentClient.existenceCheck(holderId)
                .filter(Boolean.TRUE::equals)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, holderId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + holderId + " was not found");
                });
        LOGGER.debug(USER_WAS_FOUND_LOG, holderId);

        LOGGER.info("Trying to find all active Cards linked to User with ID: {}", holderId);
        List<Card> cards = cardRepository.findAllByCardHolderUUID(holderId);
        return cards.stream()
                .filter(card -> card.getExpirationDate().isAfter(LocalDate.now()))
                .map(filteredEntity -> {
                    LOGGER.info(CARD_FOUND_LOG, filteredEntity);
                    return convertCardModelToDTO(filteredEntity);
                })
                .toList();
    }
}
