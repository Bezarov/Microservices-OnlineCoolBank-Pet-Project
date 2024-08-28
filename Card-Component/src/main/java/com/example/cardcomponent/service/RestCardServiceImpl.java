package com.example.cardcomponent.service;

import com.example.cardcomponent.dto.CardDTO;
import com.example.cardcomponent.feign.AccountComponentClient;
import com.example.cardcomponent.feign.UsersComponentClient;
import com.example.cardcomponent.model.Card;
import com.example.cardcomponent.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RestCardServiceImpl implements RestCardService {
    private static final Logger logger = LoggerFactory.getLogger(RestCardServiceImpl.class);
    private final CardRepository cardRepository;
    private final UsersComponentClient usersComponentClient;

    private final AccountComponentClient accountComponentClient;

    public RestCardServiceImpl(CardRepository cardRepository, UsersComponentClient usersComponentClient,
                               AccountComponentClient accountComponentClient) {
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
        logger.info("Trying to find Card with ID: {}", cardId);
        return cardRepository.findById(cardId)
                .map(CardEntity -> {
                    logger.info("Card was found and received to the Controller: {}", CardEntity);
                    return convertCardModelToDTO(CardEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Card with such ID was not found: {}", cardId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Card with such ID: " + cardId + " was not found");
                });
    }

    @Override
    public CardDTO getCardByCardNumber(String cardNumber) {
        logger.info("Trying to find Card with Card Number: {}", cardNumber);
        return cardRepository.findByCardNumber(cardNumber)
                .map(CardEntity -> {
                    logger.info("Card was found and received to the Controller: {}", CardEntity);
                    return convertCardModelToDTO(CardEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Card with such Card Number was not found: {}", cardNumber);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Card with such Card Number: " + cardNumber + " was not found");
                });
    }

    @Override
    public List<CardDTO> getCardsByCardHolderFullName(String cardHolderFullName) {
        logger.info("Trying to find User with Name: {}", cardHolderFullName);
        usersComponentClient.findByFullName(cardHolderFullName).orElseThrow(() -> {
            logger.error("User with such Name was not found: {}", cardHolderFullName);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User with such Full Name: " + cardHolderFullName + " was not found");
        });

        logger.info("Trying to find all Cards linked to User Name: {}", cardHolderFullName);
        List<Card> cards = cardRepository.findAllByCardHolderFullName(cardHolderFullName);
        logger.info("Cards was found and received to the Controller: {}", cards);
        return cards.stream()
                .map(this::convertCardModelToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardDTO> getAllAccountCardsByAccountId(UUID accountId) {
        logger.info("Trying to find Account with ID: {}", accountId);
        accountComponentClient.findById(accountId).orElseThrow(() -> {
            logger.error("Account with such ID was not found: {}", accountId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Account with such ID: " + accountId + " was not found");
        });

        logger.info("Trying to find all Cards linked to Account with ID: {}", accountId);
        List<Card> cards = cardRepository.findAllByAccountId(accountId);
        logger.info("Cards was found and received to the Controller: {}", cards);
        return cards.stream()
                .map(this::convertCardModelToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardDTO> getAllUserCardsByCardHolderId(UUID holderId) {
        logger.info("Trying to find User with ID: {}", holderId);
        usersComponentClient.findById(holderId).orElseThrow(() -> {
            logger.error("User with such ID was not found: {}", holderId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User with such ID: " + holderId + " was not found");
        });

        logger.info("Trying to find all Cards linked to User with ID: {}", holderId);
        List<Card> cards = cardRepository.findAllByCardHolderUUID(holderId);
        logger.info("Cards was found and received to the Controller: {}", cards);
        return cards.stream()
                .map(this::convertCardModelToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardDTO> getAllUserCardsByStatus(UUID holderId, String status) {
        logger.info("Trying to find User with ID: {}", holderId);
        usersComponentClient.findById(holderId).orElseThrow(() -> {
            logger.error("User with such ID was not found: {}", holderId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User with such ID: " + holderId + " was not found");
        });

        logger.info("Trying to find all Cards linked to User by" +
                " Card Status: {}", status);
        List<Card> cards = cardRepository.findAllByCardHolderUUIDAndStatus(holderId, status);
        logger.info("Cards was found and received to the Controller: {}", cards);
        return cards.stream()
                .map(this::convertCardModelToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardDTO> getAllExpiredCards(UUID holderId) {
        logger.info("Trying to find User with ID: {}", holderId);
        usersComponentClient.findById(holderId).orElseThrow(() -> {
            logger.error("User with such ID was not found: {}", holderId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User with such ID: " + holderId + " was not found");
        });

        logger.info("Trying to find all expired Cards linked to User with ID: {}", holderId);
        List<Card> cards = cardRepository.findAllByCardHolderUUID(holderId);
        return cards.stream()
                .filter(card -> card.getExpirationDate().isBefore(LocalDate.now()))
                .map(FilteredEntity -> {
                    logger.info("Cards was found and received to the Controller: {}", FilteredEntity);
                    return convertCardModelToDTO(FilteredEntity);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CardDTO> getAllActiveCards(UUID holderId) {
        logger.info("Trying to find User with ID: {}", holderId);
        usersComponentClient.findById(holderId).orElseThrow(() -> {
            logger.error("User with such ID was not found: {}", holderId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User with such ID: " + holderId + " was not found");
        });
        logger.info("Trying to find all active Cards linked to User with ID: {}", holderId);
        List<Card> cards = cardRepository.findAllByCardHolderUUID(holderId);
        return cards.stream()
                .filter(card -> card.getExpirationDate().isAfter(LocalDate.now()))
                .map(FilteredEntity -> {
                    logger.info("Cards was found and received to the Controller: {}", FilteredEntity);
                    return convertCardModelToDTO(FilteredEntity);
                })
                .collect(Collectors.toList());
    }
}
