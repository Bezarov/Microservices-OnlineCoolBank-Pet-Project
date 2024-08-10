package com.coolbank.service;

import com.coolbank.dto.PaymentDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentService {
    PaymentDTO createPaymentByAccounts(PaymentDTO paymentDTO);

    PaymentDTO createPaymentByCards(String fromCardNumber, String toCardNumber, BigDecimal amount);

    PaymentDTO getPaymentById(UUID paymentId);

    List<PaymentDTO> getAllAccountPaymentsByFromAccount(UUID fromAccountId);

    List<PaymentDTO> getPaymentsByStatus(UUID fromAccountId, String status);

    List<PaymentDTO> getAllAccountPaymentsByToAccount(UUID toAccountId);

    List<PaymentDTO> getAllAccountPaymentsByPaymentType(UUID fromAccountId,
                                                        String paymentType);

    List<PaymentDTO> getAllFromAccountPaymentsByPaymentDateRange(UUID fromAccountId,
                                                                 LocalDateTime fromPaymentDate,
                                                                 LocalDateTime toPaymentDate);

    List<PaymentDTO> getAllToAccountPaymentsByPaymentDateRange(UUID toAccountId,
                                                               LocalDateTime fromPaymentDate,
                                                               LocalDateTime toPaymentDate);
}
