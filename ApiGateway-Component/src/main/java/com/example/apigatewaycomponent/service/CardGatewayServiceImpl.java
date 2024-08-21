package com.example.apigatewaycomponent.service;

import com.example.apigatewaycomponent.dto.CardDTO;
import com.example.apigatewaycomponent.dto.ErrorDTO;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class CardGatewayServiceImpl implements CardGatewayService {
    private static final Logger logger = LoggerFactory.getLogger(CardGatewayServiceImpl.class);
    private static final long REQUEST_TIMEOUT = 5;
    private final KafkaTemplate<String, Object> cardKafkaTemplate;
    private final Map<String, CompletableFuture<ResponseEntity<Object>>> responseFutures = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<ResponseEntity<List<CardDTO>>>> responseListFutures = new ConcurrentHashMap<>();

    public CardGatewayServiceImpl(KafkaTemplate<String, Object> cardKafkaTemplate) {
        this.cardKafkaTemplate = cardKafkaTemplate;
    }


    @Override
    @KafkaListener(topics = "card-error", groupId = "api-gateway",
            containerFactory = "errorDTOKafkaListenerFactory")
    public void handleCardErrors(ErrorDTO cardErrorDTO, String correlationId) {
        logger.error("Received error topic with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureErrorResponse = responseFutures.remove(correlationId);
        if (futureErrorResponse != null) {
            logger.info("Complete CompletableFuture exceptionally with message: {} ", cardErrorDTO.toString());
            futureErrorResponse.completeExceptionally(new ResponseStatusException(HttpStatus.valueOf(
                    cardErrorDTO.getStatus()), cardErrorDTO.getMessage()));
        } else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> createCard(String accountId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("create-card-by-account-id", accountId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleCardCreationResponse(CardDTO cardDTO,
                                           @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(cardDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getCardById(String cardId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-card-by-id", cardId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetCardByIdResponse(CardDTO cardDTO,
                                          @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(cardDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getCardByCardNumber(String cardNumber) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-card-by-card-number", cardNumber);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetCardByNumberResponse(CardDTO cardDTO,
                                              @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(cardDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getCardsByCardHolderFullName(String cardHolderFullName) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>(
                "get-all-cards-by-holder-name", cardHolderFullName);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return getResponseEntitysCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAllCardByHolderFullNameResponse(List<CardDTO> cardDTOS,
                                                         @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = responseListFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(cardDTOS));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountCardsByAccountId(String accountId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-all-cards-by-account-id", accountId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return getResponseEntitysCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAllCardByAccountIdResponse(List<CardDTO> cardDTOS,
                                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = responseListFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(cardDTOS));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllUserCardsByCardHolderId(String holderId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-all-cards-by-holder-id", holderId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return getResponseEntitysCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAllCardByHolderIdResponse(List<CardDTO> cardDTOS,
                                                   @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = responseListFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(cardDTOS));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllUserCardsByStatus(String holderId, String status) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        Map<String, Object> getCardsRequestMap = new HashMap<>();
        getCardsRequestMap.put(holderId, status);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("update-card-status-by-id",
                getCardsRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return getResponseEntitysCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAllCardByStatusNameResponse(List<CardDTO> cardDTOS,
                                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = responseListFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(cardDTOS));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllExpiredCards(String holderId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-all-expired-cards-by-holder-id", holderId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return getResponseEntitysCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAllExpiredCardsResponse(List<CardDTO> cardDTOS,
                                                 @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = responseListFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(cardDTOS));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllActiveCards(String holderId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("get-all-active-cards-by-holder-id", holderId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return getResponseEntitysCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleGetAllActiveCardsResponse(List<CardDTO> cardDTOS,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = responseListFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(cardDTOS));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateCardStatusById(String cardId, String status) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        Map<String, Object> updateCardRequestMap = new HashMap<>();
        updateCardRequestMap.put(cardId, status);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("update-card-status-by-id",
                updateCardRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleUpdateCardStatusByIdResponse(CardDTO cardDTO,
                                                   @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(cardDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateCardStatusByCardNumber(String cardNumber, String status) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        Map<String, Object> updateCardRequestMap = new HashMap<>();
        updateCardRequestMap.put(cardNumber, status);
        ProducerRecord<String, Object> topic = new ProducerRecord<>("update-card-status-by-card-number",
                updateCardRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleUpdateCardStatusByNumberResponse(CardDTO cardDTO,
                                                       @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(cardDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteCardById(String cardId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("delete-card-by-id", cardId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleDeleteCardByCardIdResponse(CardDTO cardDTO,
                                                 @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(cardDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteAllAccountCardsByAccountId(String accountId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("delete-card-by-account-id", accountId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleDeleteCardByAccountIdResponse(CardDTO cardDTO,
                                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(cardDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteAllUsersCardsByCardHolderUUID(String cardHolderUUID) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        ProducerRecord<String, Object> topic = new ProducerRecord<>("delete-card-by-holder-id", cardHolderUUID);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return getResponseEntityCompletableFuture(correlationId, futureResponse, topic);
    }

    @Override
    public void handleDeleteAllCardsByHolderIdResponse(CardDTO cardDTO,
                                                       @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        if (futureResponse != null)
            futureResponse.complete(ResponseEntity.ok(cardDTO));
        else {
            logger.warn("Response topic with correlationId was not found: " + correlationId);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
        }
    }

    private CompletableFuture<ResponseEntity<List<Object>>> getResponseEntitysCompletableFuture(
            String correlationId, CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse,
            ProducerRecord<String, Object> topic) {
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null)
                        return ResponseEntity.ok((List<Object>) response);
                    else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
                    }
                })
                .exceptionally(error -> {
                    if (error.getCause() instanceof ResponseStatusException)
                        throw (ResponseStatusException) error.getCause();
                    else {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                    }
                });
    }

    private CompletableFuture<ResponseEntity<Object>> getResponseEntityCompletableFuture(
            String correlationId, CompletableFuture<ResponseEntity<Object>> futureResponse,
            ProducerRecord<String, Object> topic) {
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardKafkaTemplate.send(topic);

        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null)
                        return ResponseEntity.ok(response.getBody());
                    else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timed out");
                    }
                })
                .exceptionally(error -> {
                    if (error.getCause() instanceof ResponseStatusException)
                        throw (ResponseStatusException) error.getCause();
                    else {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                    }
                });
    }
}
