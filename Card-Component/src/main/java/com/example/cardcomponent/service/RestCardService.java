package com.example.cardcomponent.service;

import com.example.cardcomponent.dto.CardDTO;

import java.util.List;
import java.util.UUID;

public interface RestCardService {

    CardDTO getCardById(UUID cardId);

    CardDTO getCardByCardNumber(String cardNumber);

    List<CardDTO> getCardsByCardHolderFullName(String cardHolderFullName);

    List<CardDTO> getAllAccountCardsByAccountId(UUID accountId);

    List<CardDTO> getAllUserCardsByCardHolderId(UUID holderId);

    List<CardDTO> getAllUserCardsByStatus(UUID holderId, String status);

    List<CardDTO> getAllExpiredCards(UUID holderId);

    List<CardDTO> getAllActiveCards(UUID holderId);
}
