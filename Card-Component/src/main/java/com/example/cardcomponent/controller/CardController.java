package com.example.cardcomponent.controller;

import com.example.cardcomponent.dto.CardDTO;
import com.example.cardcomponent.service.RestCardService;
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
    private final RestCardService restCardService;

    public CardController(RestCardService restCardService) {
        this.restCardService = restCardService;
    }


    @GetMapping("/by-card-id/{cardId}")
    public ResponseEntity<CardDTO> getCardById(@PathVariable UUID cardId) {
        logger.info("Received GET request to get Card by ID: {}", cardId);
        CardDTO responseCardDTO = restCardService.getCardById(cardId);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTO);
        return ResponseEntity.ok(responseCardDTO);
    }

    @GetMapping("/by-card-number/{cardNumber}")
    public ResponseEntity<CardDTO> getCardByCardNumber(@PathVariable String cardNumber) {
        logger.info("Received GET request to get Card by Card Number: {}", cardNumber);
        CardDTO responseCardDTO = restCardService.getCardByCardNumber(cardNumber);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTO);
        return ResponseEntity.ok(responseCardDTO);
    }

    @GetMapping("/by-user-name/{cardHolderFullName}")
    public ResponseEntity<List<CardDTO>> getCardsByCardHolderFullName(@PathVariable String cardHolderFullName) {
        logger.info("Received GET request to get All Cards by Card Holder Name: {}", cardHolderFullName);
        List<CardDTO> responseCardDTOS = restCardService.getCardsByCardHolderFullName(cardHolderFullName);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTOS);
        return ResponseEntity.ok(responseCardDTOS);
    }

    @GetMapping("/by-account-id/{accountId}")
    public ResponseEntity<List<CardDTO>> getAllAccountCardsByAccountId(@PathVariable UUID accountId) {
        logger.info("Received GET request to get All Cards by Account ID: {}", accountId);
        List<CardDTO> responseCardDTO = restCardService.getAllAccountCardsByAccountId(accountId);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTO);
        return ResponseEntity.ok(responseCardDTO);
    }

    @GetMapping("/by-user-id/{holderId}")
    public ResponseEntity<List<CardDTO>> getAllUserCardsByCardHolderId(@PathVariable UUID holderId) {
        logger.info("Received GET request to get All Cards by Card Holder ID: {}", holderId);
        List<CardDTO> responseCardDTOS = restCardService.getAllUserCardsByCardHolderId(holderId);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTOS);
        return ResponseEntity.ok(responseCardDTOS);
    }

    @GetMapping("/by-user-id/{holderId}/status")
    public ResponseEntity<List<CardDTO>> getAllUserCardsByStatus(@PathVariable UUID holderId,
                                                                 @RequestParam String status) {
        logger.info("Received GET request to get All Cards by Card Holder ID: {}," +
                " with Status: {}", holderId, status);
        List<CardDTO> responseCardDTOS = restCardService.getAllUserCardsByStatus(holderId, status);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTOS);
        return ResponseEntity.ok(responseCardDTOS);
    }

    @GetMapping("/expired/by-user-id/{holderId}")
    public ResponseEntity<List<CardDTO>> getAllExpiredCard(@PathVariable UUID holderId) {
        logger.info("Received GET request to get All Expired Cards by Card Holder ID: {}", holderId);
        List<CardDTO> responseCardDTOS = restCardService.getAllExpiredCards(holderId);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTOS);
        return ResponseEntity.ok(responseCardDTOS);
    }

    @GetMapping("/active/by-user-id/{holderId}")
    public ResponseEntity<List<CardDTO>> getAllActiveCards(@PathVariable UUID holderId) {
        logger.info("Received GET request to get All Active Cards by Card Holder ID: {}", holderId);
        List<CardDTO> responseCardDTOS = restCardService.getAllActiveCards(holderId);
        logger.debug("Request was successfully processed and response was sent: {}", responseCardDTOS);
        return ResponseEntity.ok(responseCardDTOS);
    }
}
