package com.example.paymentcomponent.repository;

import com.example.paymentcomponent.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findAllByFromAccountId(UUID fromAccountId);

    List<Payment> findAllByToAccountId(UUID toAccountId);

    List<Payment> findAllByPaymentType(String paymentType);

    List<Payment> findAllByFromAccountIdAndPaymentDateBetween(UUID fromAccountId,
                                                              LocalDateTime fromPaymentDate,
                                                              LocalDateTime toPaymentDate);

    List<Payment> findAllByToAccountIdAndPaymentDateBetween(UUID toAccountId,
                                                            LocalDateTime fromPaymentDate,
                                                            LocalDateTime toPaymentDate);

}
