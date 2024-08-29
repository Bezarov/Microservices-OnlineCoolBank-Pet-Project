package com.example.paymentcomponent.feign;

import com.example.paymentcomponent.dto.CardDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CardComponentClientFallback implements CardComponentClient {
    @Override
    public List<CardDTO> findAllCardsByAccountId(UUID cardId) {
        return null;
    }

    @Override
    public Optional<CardDTO> findByCardNumber(String cardNumber) {
        return Optional.empty();
    }
}
