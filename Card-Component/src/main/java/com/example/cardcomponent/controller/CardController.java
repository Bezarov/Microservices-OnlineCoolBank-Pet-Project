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
@RequestMapping("/card")
public class CardController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CardController.class);
    private static final String RESPONSE_LOG = "Request was successfully processed and response was sent: {}";

    private final RestCardService restCardService;

    public CardController(RestCardService restCardService) {
        this.restCardService = restCardService;
    }


    @GetMapping("/by-card-id/{cardId}")
    public ResponseEntity<CardDTO> getCardById(@PathVariable UUID cardId) {
        LOGGER.debug("Received GET request to get Card by ID: {}", cardId);
        CardDTO responseCardDTO = restCardService.getCardById(cardId);
        LOGGER.debug(RESPONSE_LOG, responseCardDTO);
        return ResponseEntity.ok(responseCardDTO);
    }

    @GetMapping("/by-card-number/{cardNumber}")
    public ResponseEntity<CardDTO> getCardByCardNumber(@PathVariable String cardNumber) {
        LOGGER.debug("Received GET request to get Card by Card Number: {}", cardNumber);
        CardDTO responseCardDTO = restCardService.getCardByCardNumber(cardNumber);
        LOGGER.debug(RESPONSE_LOG, responseCardDTO);
        return ResponseEntity.ok(responseCardDTO);
    }

    @GetMapping("/by-user-name/{cardHolderFullName}")
    public ResponseEntity<List<CardDTO>> getCardsByCardHolderFullName(@PathVariable String cardHolderFullName) {
        LOGGER.debug("Received GET request to get All Cards by Card Holder Name: {}", cardHolderFullName);
        List<CardDTO> responseCardDTOS = restCardService.getCardsByCardHolderFullName(cardHolderFullName);
        LOGGER.debug(RESPONSE_LOG, responseCardDTOS);
        return ResponseEntity.ok(responseCardDTOS);
    }

    @GetMapping("/by-account-id/{accountId}")
    public ResponseEntity<List<CardDTO>> getAllAccountCardsByAccountId(@PathVariable UUID accountId) {
        LOGGER.debug("Received GET request to get All Cards by Account ID: {}", accountId);
        List<CardDTO> responseCardDTO = restCardService.getAllAccountCardsByAccountId(accountId);
        LOGGER.debug(RESPONSE_LOG, responseCardDTO);
        return ResponseEntity.ok(responseCardDTO);
    }

    @GetMapping("/by-user-id/{holderId}")
    public ResponseEntity<List<CardDTO>> getAllUserCardsByCardHolderId(@PathVariable UUID holderId) {
        LOGGER.debug("Received GET request to get All Cards by Card Holder ID: {}", holderId);
        List<CardDTO> responseCardDTOS = restCardService.getAllUserCardsByCardHolderId(holderId);
        LOGGER.debug(RESPONSE_LOG, responseCardDTOS);
        return ResponseEntity.ok(responseCardDTOS);
    }

    @GetMapping("/by-user-id/{holderId}/status")
    public ResponseEntity<List<CardDTO>> getAllUserCardsByStatus(@PathVariable UUID holderId,
                                                                 @RequestParam String status) {
        LOGGER.debug("Received GET request to get All Cards by Card Holder ID: {}," +
                " with Status: {}", holderId, status);
        List<CardDTO> responseCardDTOS = restCardService.getAllUserCardsByStatus(holderId, status);
        LOGGER.debug(RESPONSE_LOG, responseCardDTOS);
        return ResponseEntity.ok(responseCardDTOS);
    }

    @GetMapping("/expired/by-user-id/{holderId}")
    public ResponseEntity<List<CardDTO>> getAllExpiredCard(@PathVariable UUID holderId) {
        LOGGER.debug("Received GET request to get All Expired Cards by Card Holder ID: {}", holderId);
        List<CardDTO> responseCardDTOS = restCardService.getAllExpiredCards(holderId);
        LOGGER.debug(RESPONSE_LOG, responseCardDTOS);
        return ResponseEntity.ok(responseCardDTOS);
    }

    @GetMapping("/active/by-user-id/{holderId}")
    public ResponseEntity<List<CardDTO>> getAllActiveCards(@PathVariable UUID holderId) {
        LOGGER.debug("Received GET request to get All Active Cards by Card Holder ID: {}", holderId);
        List<CardDTO> responseCardDTOS = restCardService.getAllActiveCards(holderId);
        LOGGER.debug(RESPONSE_LOG, responseCardDTOS);
        return ResponseEntity.ok(responseCardDTOS);
    }
}
