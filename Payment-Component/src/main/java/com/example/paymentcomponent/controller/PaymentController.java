package com.example.paymentcomponent.controller;

import com.example.paymentcomponent.dto.PaymentDTO;
import com.example.paymentcomponent.service.RestPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentController.class);
    private final RestPaymentService restPaymentService;

    public PaymentController(RestPaymentService restPaymentService) {
        this.restPaymentService = restPaymentService;
    }

    @GetMapping("/by-payment-id/{paymentId}")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable UUID paymentId) {
        LOGGER.debug("Received GET request to get Payment by ID: {}", paymentId);
        PaymentDTO responsePaymentDTO = restPaymentService.getPaymentById(paymentId);
        LOGGER.debug("Request was successfully processed and response was sent: {}", responsePaymentDTO);
        return ResponseEntity.ok(responsePaymentDTO);
    }
}