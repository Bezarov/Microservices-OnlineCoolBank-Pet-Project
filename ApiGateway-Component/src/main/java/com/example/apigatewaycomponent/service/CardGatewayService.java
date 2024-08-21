package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.AccountDTO;
import com.example.apigatewaycomponent.dto.CardDTO;
import com.example.apigatewaycomponent.dto.ErrorDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CardGatewayService {
    void handleCardErrors(ErrorDTO cardErrorDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> createCard(String accountId);

    void handleCardCreationResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getCardById(String cardId);

    void handleGetCardByIdResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> getCardByCardNumber(String cardNumber);

    void handleGetCardByNumberResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getCardsByCardHolderFullName(String cardHolderFullName);

    void handleGetAllCardByHolderFullNameResponse(List<CardDTO> cardDTOS,
                                                  @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllAccountCardsByAccountId(String accountId);

    void handleGetAllCardByAccountIdResponse(List<CardDTO> cardDTOS,
                                             @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllUserCardsByCardHolderId(String holderId);

    void handleGetAllCardByHolderIdResponse(List<CardDTO> cardDTOS,
                                            @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllUserCardsByStatus(String holderId, String status);

    void handleGetAllCardByStatusNameResponse(List<CardDTO> cardDTOS,
                                              @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllExpiredCards(String holderId);

    void handleGetAllExpiredCardsResponse(List<CardDTO> cardDTOS,
                                          @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<List<Object>>> getAllActiveCards(String holderId);

    void handleGetAllActiveCardsResponse(List<CardDTO> cardDTOS,
                                         @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updateCardStatusById(String cardId, String status);

    void handleUpdateCardStatusByIdResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> updateCardStatusByCardNumber(String cardNumber, String status);

    void handleUpdateCardStatusByNumberResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> deleteCardById(String cardId);

    void handleDeleteCardByCardIdResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> deleteAllAccountCardsByAccountId(String accountId);

    void handleDeleteCardByAccountIdResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    CompletableFuture<ResponseEntity<Object>> deleteAllUsersCardsByCardHolderUUID(String cardHolderUUID);

    void handleDeleteAllCardsByHolderIdResponse(CardDTO cardDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);
}
