package com.example.paymentcomponent.service;

import com.example.paymentcomponent.dto.PaymentDTO;

import java.util.UUID;

public interface RestPaymentService {

    PaymentDTO getPaymentById(UUID paymentId);
}
