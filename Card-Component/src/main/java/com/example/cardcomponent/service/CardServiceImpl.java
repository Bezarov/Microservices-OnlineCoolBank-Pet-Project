package com.example.cardcomponent.service;

import com.example.cardcomponent.dto.CardDTO;
import com.example.cardcomponent.model.Card;
import com.example.cardcomponent.repository.CardRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CardServiceImpl implements CardService {
    private static final Logger logger = LoggerFactory.getLogger(CardServiceImpl.class);
    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final UsersRepository usersRepository;
    private final Random random = new Random();

    @Autowired
    public CardServiceImpl(CardRepository cardRepository,
                           AccountRepository accountRepository, UsersRepository usersRepository) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.usersRepository = usersRepository;
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

    private Card cardGenerator(Account account) {
        Card card = new Card();
        card.setAccount(account);
        card.setCardHolderUUID(account.getUser().getId());
        logger.debug("Generating card number");
        card.setCardNumber(cardNumberGenerator());
        card.setCardHolderFullName(account.getAccountHolderFullName());
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
        logger.debug("Card number generated successfully: {}", cardNumber);
        return cardNumber.toString();
    }

    @Override
    public CardDTO createCard(UUID accountId) {
        logger.info("Trying to find Account with ID: {}", accountId);
        Account accountEntity = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    logger.error("Account with such ID was not found: {}, ", accountId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found");
                });

        logger.debug("Generating Card");
        Card card = cardRepository.save(cardGenerator(accountEntity));
        logger.info("Card generated successfully: {}", card);
        return convertCardModelToDTO(card);
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
        usersRepository.findByFullName(cardHolderFullName).orElseThrow(() -> {
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
        accountRepository.findById(accountId).orElseThrow(() -> {
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
        usersRepository.findById(holderId).orElseThrow(() -> {
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
        usersRepository.findById(holderId).orElseThrow(() -> {
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
        usersRepository.findById(holderId).orElseThrow(() -> {
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
        usersRepository.findById(holderId).orElseThrow(() -> {
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

    @Override
    public CardDTO updateCardStatusById(UUID cardId, String status) {
        logger.info("Trying to find Card with ID: {}", cardId);
        return cardRepository.findById(cardId)
                .map(CardEntity -> {
                    CardEntity.setStatus(status);
                    cardRepository.save(CardEntity);
                    logger.info("Card status was updated and received to the Controller: {}", CardEntity);
                    return convertCardModelToDTO(CardEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Card with such ID was not found: {}", cardId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Card with such ID: " + cardId + " was not found");
                });
    }

    @Override
    public CardDTO updateCardStatusByCardNumber(String cardNumber, String status) {
        logger.info("Trying to find Card with Card Number: {}", cardNumber);
        return cardRepository.findByCardNumber(cardNumber)
                .map(CardEntity -> {
                    CardEntity.setStatus(status);
                    cardRepository.save(CardEntity);
                    logger.info("Card status was updated and received to the Controller: {}", CardEntity);
                    return convertCardModelToDTO(CardEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Card with such Card Number was not found: {}", cardNumber);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Card with such Card Number: " + cardNumber + " was not found");
                });
    }

    @Transactional
    @Override
    public ResponseEntity<String> deleteCardById(UUID cardId) {
        logger.info("Trying to find Card with ID: {}", cardId);
        cardRepository.findById(cardId)
                .map(CardEntity -> {
                    CardEntity.setStatus("DEACTIVATED");
                    logger.info("Cards Status was changed to - DEACTIVATED: {}", CardEntity);
                    return cardRepository.save(CardEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Card with such ID was not found: {}", cardId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Card with such ID: " + cardId + " was not found");
                });
        return new ResponseEntity<>("Card deleted successfully", HttpStatus.ACCEPTED);
    }

    @Transactional
    @Override
    public ResponseEntity<String> deleteAllAccountCardsByAccountId(UUID accountId) {
        logger.info("Trying to find Account with ID: {}", accountId);
        accountRepository.findById(accountId).orElseThrow(() -> {
            logger.error("Account with such ID was not found: {}", accountId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Account with such ID: " + accountId + " was not found");
        });

        logger.info("Trying to find Account Cards with Account ID: {}", accountId);
        cardRepository.findAllByAccountId(accountId)
                .forEach(CardEntity -> {
                    CardEntity.setStatus("DEACTIVATED");
                    logger.info("Account Cards Status was changed to - DEACTIVATED: {}", CardEntity);
                });
        return new ResponseEntity<>("Cards deleted successfully", HttpStatus.ACCEPTED);
    }

    @Transactional
    @Override
    public ResponseEntity<String> deleteAllUsersCardsByCardHolderUUID(UUID cardHolderUUID) {
        logger.info("Trying to find User with ID: {}", cardHolderUUID);
        usersRepository.findById(cardHolderUUID).orElseThrow(() -> {
            logger.error("User with such ID was not found: {}", cardHolderUUID);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User with such ID: " + cardHolderUUID + " was not found");
        });

        logger.info("Trying to find User Cards with User ID: {}", cardHolderUUID);
        cardRepository.findAllByCardHolderUUID(cardHolderUUID)
                .forEach(CardEntity -> {
                    CardEntity.setStatus("DEACTIVATED");
                    logger.info("Account Cards Status was changed to - DEACTIVATED: {}", CardEntity);
                });
        return new ResponseEntity<>("Cards deleted successfully", HttpStatus.ACCEPTED);
    }
}
