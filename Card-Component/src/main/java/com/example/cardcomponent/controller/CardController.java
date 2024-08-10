package com.example.cardcomponent.controller;

import com.example.cardcomponent.dto.CardDTO;
import com.example.cardcomponent.service.CardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cards")
public class CardController {
    private static final Logger logger = LoggerFactory.getLogger(CardController.class);
    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/by-account-id/{accountId}")
    public ResponseEntity<CardDTO> createCard(@PathVariable UUID accountId) {
        logger.info("Received POST request to create Card for Account with ID: {}", accountId);
        CardDTO responseCardDTO = cardService.createCard(accountId);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTO);
        return ResponseEntity.ok(responseCardDTO);
    }

    @GetMapping("/by-card-id/{cardId}")
    public ResponseEntity<CardDTO> getCardById(@PathVariable UUID cardId) {
        logger.info("Received GET request to get Card by ID: {}", cardId);
        CardDTO responseCardDTO = cardService.getCardById(cardId);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTO);
        return ResponseEntity.ok(responseCardDTO);
    }

    @GetMapping("/by-card-number/{cardNumber}")
    public ResponseEntity<CardDTO> getCardByCardNumber(@PathVariable String cardNumber) {
        logger.info("Received GET request to get Card by Card Number: {}", cardNumber);
        CardDTO responseCardDTO = cardService.getCardByCardNumber(cardNumber);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTO);
        return ResponseEntity.ok(responseCardDTO);
    }

    @GetMapping("/by-user-name/{cardHolderFullName}")
    public ResponseEntity<List<CardDTO>> getCardsByCardHolderFullName(@PathVariable String cardHolderFullName) {
        logger.info("Received GET request to get All Cards by Card Holder Name: {}", cardHolderFullName);
        List<CardDTO> responseCardDTOS = cardService.getCardsByCardHolderFullName(cardHolderFullName);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTOS);
        return ResponseEntity.ok(responseCardDTOS);
    }

    @GetMapping("/by-account-id/{accountId}")
    public ResponseEntity<List<CardDTO>> getAllAccountCardsByAccountId(@PathVariable UUID accountId) {
        logger.info("Received GET request to get All Cards by Account ID: {}", accountId);
        List<CardDTO> responseCardDTO = cardService.getAllAccountCardsByAccountId(accountId);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTO);
        return ResponseEntity.ok(responseCardDTO);
    }

    @GetMapping("/by-user-id/{holderId}")
    public ResponseEntity<List<CardDTO>> getAllUserCardsByCardHolderId(@PathVariable UUID holderId) {
        logger.info("Received GET request to get All Cards by Card Holder ID: {}", holderId);
        List<CardDTO> responseCardDTOS = cardService.getAllUserCardsByCardHolderId(holderId);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTOS);
        return ResponseEntity.ok(responseCardDTOS);
    }

    @GetMapping("/by-user-id/{holderId}/status")
    public ResponseEntity<List<CardDTO>> getAllUserCardsByStatus(@PathVariable UUID holderId,
                                                                 @RequestParam String status) {
        logger.info("Received GET request to get All Cards by Card Holder ID: {}," +
                " with Status: {}", holderId, status);
        List<CardDTO> responseCardDTOS = cardService.getAllUserCardsByStatus(holderId, status);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTOS);
        return ResponseEntity.ok(responseCardDTOS);
    }

    @GetMapping("/expired/by-user-id/{holderId}")
    public ResponseEntity<List<CardDTO>> getAllExpiredCard(@PathVariable UUID holderId) {
        logger.info("Received GET request to get All Expired Cards by Card Holder ID: {}", holderId);
        List<CardDTO> responseCardDTOS = cardService.getAllExpiredCards(holderId);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTOS);
        return ResponseEntity.ok(responseCardDTOS);
    }

    @GetMapping("/active/by-user-id/{holderId}")
    public ResponseEntity<List<CardDTO>> getAllActiveCards(@PathVariable UUID holderId) {
        logger.info("Received GET request to get All Active Cards by Card Holder ID: {}", holderId);
        List<CardDTO> responseCardDTOS = cardService.getAllActiveCards(holderId);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTOS);
        return ResponseEntity.ok(responseCardDTOS);
    }

    @PatchMapping("/by-card-id/{cardId}/status")
    public ResponseEntity<CardDTO> updateCardStatusById(@PathVariable UUID cardId,
                                                        @RequestParam String status) {
        logger.info("Received PATCH request to update Status of Card by Card ID: {}," +
                " with Status: {}", cardId, status);
        CardDTO responseCardDTO = cardService.updateCardStatusById(cardId, status);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTO);
        return ResponseEntity.ok(responseCardDTO);
    }

    @PatchMapping("/by-card-number/{cardNumber}/status")
    public ResponseEntity<CardDTO> updateCardStatusByCardNumber(@PathVariable String cardNumber,
                                                                @RequestParam String status) {
        logger.info("Received PATCH request to update Status of Card by " +
                "Card Card Number: {}, with Status: {}", cardNumber, status);
        CardDTO responseCardDTO = cardService.updateCardStatusByCardNumber(cardNumber, status);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTO);
        return ResponseEntity.ok(responseCardDTO);
    }

    @DeleteMapping("/by-card-id/{cardId}")
    public ResponseEntity<String> deleteCardById(@PathVariable UUID cardId) {
        logger.info("Received DELETE request to remove Card with ID: {}", cardId);
        ResponseEntity<String> responseMessage = cardService.deleteCardById(cardId);
        logger.debug("Request was successfully processed and response message was sent: {}", responseMessage);
        return responseMessage;
    }

    @DeleteMapping("/by-account-id/{accountId}")
    public ResponseEntity<String> deleteAllAccountCardsByAccountId(@PathVariable UUID accountId) {
        logger.info("Received DELETE request to remove All Account Cards" +
                " by Account ID: {}", accountId);
        ResponseEntity<String> responseMessage = cardService.deleteAllAccountCardsByAccountId(accountId);
        logger.debug("Request was successfully processed and response message was sent: {}", responseMessage);
        return responseMessage;
    }

    @DeleteMapping("/by-user-id/{cardHolderUUID}")
    public ResponseEntity<String> deleteAllUsersCardsByCardHolderUUID(@PathVariable UUID cardHolderUUID) {
        logger.info("Received DELETE request to remove All User Cards" +
                " by Holder ID: {}", cardHolderUUID);
        ResponseEntity<String> responseMessage = cardService.deleteAllUsersCardsByCardHolderUUID(cardHolderUUID);
        logger.debug("Request was successfully processed and response message was sent: {}", responseMessage);
        return responseMessage;
    }
}
