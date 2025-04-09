package com.example.apigatewaycomponent.controller;

import com.example.apigatewaycomponent.dto.PaymentDTO;
import com.example.apigatewaycomponent.service.PaymentGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/payment")
public class PaymentGatewayController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentGatewayController.class);
    private static final String RESPONSE_LOG = "Request was successfully processed and response was sent: {}";

    private final PaymentGatewayService paymentGatewayService;

    public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
    }

    @PostMapping("/account/transfer")
    public CompletableFuture<ResponseEntity<Object>> createPaymentByAccounts(@RequestBody PaymentDTO paymentDTO) {
        LOGGER.debug("Received POST request for Transfer from Account to Account: {}", paymentDTO);
        return paymentGatewayService.createPaymentByAccounts(paymentDTO)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @PostMapping("/card/transfer")
    public CompletableFuture<ResponseEntity<Object>> createPaymentByCards(
            @RequestParam(name = "from-card-number") String fromCardNumber,
            @RequestParam(name = "to-card-number") String toCardNumber,
            @RequestParam(name = "amount") BigDecimal amount) {
        LOGGER.debug("Received POST request to Card to Card Transfer: From Card: {}," +
                " To Card: {}, in AMOUNT OF: {}", fromCardNumber, toCardNumber, amount);
        return paymentGatewayService.createPaymentByCards(fromCardNumber, toCardNumber, amount)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/by-payment-id/{paymentId}")
    public CompletableFuture<ResponseEntity<Object>> getPaymentById(@PathVariable UUID paymentId) {
        LOGGER.debug("Received GET request to get Payment by ID: {}", paymentId);
        return paymentGatewayService.getPaymentById(paymentId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/from-account-id/{fromAccountId}")
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountPaymentsByFromAccount(
            @PathVariable UUID fromAccountId) {
        LOGGER.debug("Received GET request to get All Payments from Account ID: {}", fromAccountId);
        return paymentGatewayService.getAllAccountPaymentsByFromAccount(fromAccountId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/to-account-id/{toAccountId}")
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountPaymentsByToAccount(
            @PathVariable UUID toAccountId) {
        LOGGER.debug("Received GET request to get All Payments to Account ID: {}", toAccountId);
        return paymentGatewayService.getAllAccountPaymentsByToAccount(toAccountId)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/from-account-id/{fromAccountId}/status")
    public CompletableFuture<ResponseEntity<List<Object>>> getPaymentsByStatus(
            @PathVariable UUID fromAccountId,
            @RequestParam String status) {
        LOGGER.debug("Received GET request to get All Payments from Account ID: {}," +
                " with Status: {}", fromAccountId, status);
        return paymentGatewayService.getPaymentsByStatus(fromAccountId, status)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/from-account-id/{fromAccountId}/type")
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountPaymentsByPaymentType(
            @PathVariable UUID fromAccountId,
            @RequestParam(name = "payment-type") String paymentType) {
        LOGGER.debug("Received GET request to get All Payments from Account ID: {}," +
                " with Type: {}", fromAccountId, paymentType);
        return paymentGatewayService.getAllAccountPaymentsByPaymentType(fromAccountId, paymentType)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/from-account-id/{fromAccountId}/date-range")
    public CompletableFuture<ResponseEntity<List<Object>>> getAllFromAccountPaymentsByPaymentDateRange(
            @PathVariable UUID fromAccountId,
            @RequestParam(name = "from-date") LocalDateTime fromPaymentDate,
            @RequestParam(name = "to-date") LocalDateTime toPaymentDate) {
        LOGGER.debug("Received GET request to get All Account Payments in date range FROM" +
                " Account with ID: {}, from: {}, to: {} ", fromAccountId, fromPaymentDate, toPaymentDate);
        return paymentGatewayService.getAllFromAccountPaymentsByPaymentDateRange(fromAccountId, fromPaymentDate, toPaymentDate)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }

    @GetMapping("/to-account-id/{toAccountId}/date-range")
    public CompletableFuture<ResponseEntity<List<Object>>> getAllFromToPaymentsByPaymentDateRange(
            @PathVariable UUID toAccountId,
            @RequestParam(name = "from-date") LocalDateTime fromPaymentDate,
            @RequestParam(name = "to-date") LocalDateTime toPaymentDate) {
        LOGGER.debug("Received GET request to get All Account Payments in date range TO" +
                " Account with ID: {}, from: {}, to: {} ", toPaymentDate, fromPaymentDate, toPaymentDate);
        return paymentGatewayService.getAllToAccountPaymentsByPaymentDateRange(
                        toAccountId, fromPaymentDate, toPaymentDate)
                .thenApply(response -> {
                    LOGGER.debug(RESPONSE_LOG, response);
                    return response;
                });
    }
}
