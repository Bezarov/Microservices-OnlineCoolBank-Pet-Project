package com.example.paymentcomponent.service;

import com.example.paymentcomponent.dto.PaymentDTO;
import com.example.paymentcomponent.model.Payment;
import com.example.paymentcomponent.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class RestPaymentServiceImpl implements RestPaymentService {
    private static final Logger logger = LoggerFactory.getLogger(RestPaymentServiceImpl.class);
    private final PaymentRepository paymentRepository;

    public RestPaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }


    private PaymentDTO convertPaymentModelToDTO(Payment payment) {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setId(payment.getId());
        paymentDTO.setFromAccount(payment.getFromAccountId());
        paymentDTO.setToAccount(payment.getToAccountId());
        paymentDTO.setPaymentDate(payment.getPaymentDate());
        paymentDTO.setAmount(payment.getAmount());
        paymentDTO.setStatus(payment.getStatus());
        paymentDTO.setPaymentType(payment.getPaymentType());
        paymentDTO.setDescription(payment.getDescription());
        return paymentDTO;
    }

    @Override
    public PaymentDTO getPaymentById(UUID paymentId) {
        logger.info("Trying to find Payment with ID: {}", paymentId);
        return paymentRepository.findById(paymentId)
                .map(paymentEntity -> {
                    logger.info("Payment was found and received to the Controller: {}", paymentEntity);
                    return convertPaymentModelToDTO(paymentEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Payment with such ID: {} was not found", paymentId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Payment with such ID was NOT Found: " + paymentId);
                });
    }
}