package com.example.cardcomponent.service;

import com.example.cardcomponent.dto.CardDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface CardService {
    CardDTO createCard(UUID accountId);

    CardDTO getCardById(UUID cardId);

    CardDTO getCardByCardNumber(String cardNumber);

    List<CardDTO> getCardsByCardHolderFullName(String cardHolderFullName);

    List<CardDTO> getAllAccountCardsByAccountId(UUID accountId);

    List<CardDTO> getAllUserCardsByCardHolderId(UUID holderId);

    List<CardDTO> getAllUserCardsByStatus(UUID holderId, String status);

    List<CardDTO> getAllExpiredCards(UUID holderId);

    List<CardDTO> getAllActiveCards(UUID holderId);

    CardDTO updateCardStatusById(UUID cardId, String status);

    CardDTO updateCardStatusByCardNumber(String cardNumber, String status);

    ResponseEntity<String> deleteCardById(UUID cardId);

    ResponseEntity<String> deleteAllAccountCardsByAccountId(UUID accountId);

    ResponseEntity<String> deleteAllUsersCardsByCardHolderUUID(UUID cardHolderUUID);
}
