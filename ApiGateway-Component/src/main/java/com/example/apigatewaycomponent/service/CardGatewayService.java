package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.CardDTO;
import com.example.apigatewaycomponent.dto.ErrorDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface CardGatewayService {
    void handleCardErrors(ErrorDTO cardErrorDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> createCard(UUID accountId);

    void handleCardCreationResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getCardById(UUID cardId);

    void handleGetCardByIdResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getCardByCardNumber(String cardNumber);

    void handleGetCardByNumberResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getCardsByCardHolderFullName(String cardHolderFullName);

    void handleGetAllCardByHolderFullNameResponse(List<CardDTO> cardDTOS,
                                                  @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllAccountCardsByAccountId(UUID accountId);

    void handleGetAllCardByAccountIdResponse(List<CardDTO> cardDTOS,
                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllUserCardsByCardHolderId(UUID holderId);

    void handleGetAllCardByHolderIdResponse(List<CardDTO> cardDTOS,
                                            @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllUserCardsByStatus(UUID holderId, String status);

    void handleGetAllCardByStatusNameResponse(List<CardDTO> cardDTOS,
                                              @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllExpiredCards(UUID holderId);

    void handleGetAllExpiredCardsResponse(List<CardDTO> cardDTOS,
                                          @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllActiveCards(UUID holderId);

    void handleGetAllActiveCardsResponse(List<CardDTO> cardDTOS,
                                         @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updateCardStatusById(UUID cardId, String status);

    void handleUpdateCardStatusByIdResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updateCardStatusByCardNumber(String cardNumber, String status);

    void handleUpdateCardStatusByNumberResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> deleteCardById(UUID cardId);

    void handleDeleteCardByCardIdResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> deleteAllAccountCardsByAccountId(UUID accountId);

    void handleDeleteCardByAccountIdResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> deleteAllUsersCardsByCardHolderUUID(UUID cardHolderUUID);

    void handleDeleteAllCardsByHolderIdResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);
}
