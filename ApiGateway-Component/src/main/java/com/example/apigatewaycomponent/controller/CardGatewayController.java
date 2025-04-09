package com.example.apigatewaycomponent.controller;

import com.example.apigatewaycomponent.service.CardGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/card")
public class CardGatewayController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CardGatewayController.class);
    private static final String RESPONSE_LOG = "Request was successfully processed and response was sent: {}";

    private final CardGatewayService cardGatewayService;

    public CardGatewayController(CardGatewayService cardGatewayService) {
        this.cardGatewayService = cardGatewayService;
    }

    @PostMapping("/by-account-id/{accountId}")
    public CompletableFuture<ResponseEntity<Object>> createCard(@PathVariable UUID accountId) {
        LOGGER.debug("Received POST request to create Card for Account with ID: {}", accountId);
        return cardGatewayService.createCard(accountId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/by-card-id/{cardId}")
    public CompletableFuture<ResponseEntity<Object>> getCardById(@PathVariable UUID cardId) {
        LOGGER.debug("Received GET request to get Card by ID: {}", cardId);
        return cardGatewayService.getCardById(cardId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/by-card-number/{cardNumber}")
    public CompletableFuture<ResponseEntity<Object>> getCardByCardNumber(@PathVariable String cardNumber) {
        LOGGER.debug("Received GET request to get Card by Card Number: {}", cardNumber);
        return cardGatewayService.getCardByCardNumber(cardNumber)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/by-user-name/{cardHolderFullName}")
    public CompletableFuture<ResponseEntity<List<Object>>> getCardsByCardHolderFullName(
            @PathVariable String cardHolderFullName) {
        LOGGER.debug("Received GET request to get All Cards by Card Holder Name: {}", cardHolderFullName);
        return cardGatewayService.getCardsByCardHolderFullName(cardHolderFullName)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/by-account-id/{accountId}")
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountCardsByAccountId(
            @PathVariable UUID accountId) {
        LOGGER.debug("Received GET request to get All Cards by Account ID: {}", accountId);
        return cardGatewayService.getAllAccountCardsByAccountId(accountId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/by-user-id/{holderId}")
    public CompletableFuture<ResponseEntity<List<Object>>> getAllUserCardsByCardHolderId(
            @PathVariable UUID holderId) {
        LOGGER.debug("Received GET request to get All Cards by Card Holder ID: {}", holderId);
        return cardGatewayService.getAllUserCardsByCardHolderId(holderId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/by-user-id/{holderId}/status")
    public CompletableFuture<ResponseEntity<List<Object>>> getAllUserCardsByStatus(@PathVariable UUID holderId,
                                                                                   @RequestParam String status) {
        LOGGER.debug("Received GET request to get All Cards by Card Holder ID: {}," +
                " with Status: {}", holderId, status);
        return cardGatewayService.getAllUserCardsByStatus(holderId, status)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/expired/by-user-id/{holderId}")
    public CompletableFuture<ResponseEntity<List<Object>>> getAllExpiredCard(@PathVariable UUID holderId) {
        LOGGER.debug("Received GET request to get All Expired Cards by Card Holder ID: {}", holderId);
        return cardGatewayService.getAllExpiredCards(holderId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/active/by-user-id/{holderId}")
    public CompletableFuture<ResponseEntity<List<Object>>> getAllActiveCards(@PathVariable UUID holderId) {
        LOGGER.debug("Received GET request to get All Active Cards by Card Holder ID: {}", holderId);
        return cardGatewayService.getAllActiveCards(holderId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @PatchMapping("/by-card-id/{cardId}/status")
    public CompletableFuture<ResponseEntity<Object>> updateCardStatusById(@PathVariable UUID cardId,
                                                                          @RequestParam String status) {
        LOGGER.debug("Received PATCH request to update Status of Card by Card ID: {}," +
                " with Status: {}", cardId, status);
        return cardGatewayService.updateCardStatusById(cardId, status)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @PatchMapping("/by-card-number/{cardNumber}/status")
    public CompletableFuture<ResponseEntity<Object>> updateCardStatusByCardNumber(@PathVariable String cardNumber,
                                                                                  @RequestParam String status) {
        LOGGER.debug("Received PATCH request to update Status of Card by " +
                "Card Card Number: {}, with Status: {}", cardNumber, status);
        return cardGatewayService.updateCardStatusByCardNumber(cardNumber, status)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @DeleteMapping("/by-card-id/{cardId}")
    public CompletableFuture<ResponseEntity<Object>> deleteCardById(@PathVariable UUID cardId) {
        LOGGER.debug("Received DELETE request to remove Card with ID: {}", cardId);
        return cardGatewayService.deleteCardById(cardId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @DeleteMapping("/by-account-id/{accountId}")
    public CompletableFuture<ResponseEntity<Object>> deleteAllAccountCardsByAccountId(
            @PathVariable UUID accountId) {
        LOGGER.debug("Received DELETE request to remove All Account Cards" +
                " by Account ID: {}", accountId);
        return cardGatewayService.deleteAllAccountCardsByAccountId(accountId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @DeleteMapping("/by-user-id/{cardHolderUUID}")
    public CompletableFuture<ResponseEntity<Object>> deleteAllUsersCardsByCardHolderUUID(
            @PathVariable UUID cardHolderUUID) {
        LOGGER.debug("Received DELETE request to remove All User Cards" +
                " by Holder ID: {}", cardHolderUUID);
        return cardGatewayService.deleteAllUsersCardsByCardHolderUUID(cardHolderUUID)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }
}
