package com.example.cardcomponent.service;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.util.UUID;

public interface KafkaCardService {
    void createCard(UUID accountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getCardById(UUID cardId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getCardByCardNumber(String cardNumber, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getCardsByCardHolderFullName(String cardHolderFullName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getAllAccountCardsByAccountId(UUID accountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getAllUserCardsByCardHolderId(UUID holderId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getAllUserCardsByStatus(UUID holderId, String status, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getAllExpiredCards(UUID holderId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getAllActiveCards(UUID holderId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void updateCardStatusById(UUID cardId, String status, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void updateCardStatusByCardNumber(String cardNumber, String status, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void deleteCardById(UUID cardId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void deleteAllAccountCardsByAccountId(UUID accountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void deleteAllUsersCardsByCardHolderUUID(UUID cardHolderUUID, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);
}
