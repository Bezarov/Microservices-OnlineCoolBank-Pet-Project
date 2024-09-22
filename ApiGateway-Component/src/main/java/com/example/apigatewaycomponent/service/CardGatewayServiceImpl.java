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
    private final KafkaTemplate<String, UUID> uuidKafkaTemplate;
    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private final KafkaTemplate<String, Map<UUID, String>> mapUUIDToStringKafkaTemplate;
    private final KafkaTemplate<String, Map<String, String>> mapStringToStringKafkaTemplate;

    private final Map<String, CompletableFuture<ResponseEntity<Object>>> responseFutures = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<ResponseEntity<List<CardDTO>>>> responseListFutures = new ConcurrentHashMap<>();

    public CardGatewayServiceImpl(KafkaTemplate<String, UUID> uuidKafkaTemplate, KafkaTemplate<String, String> stringKafkaTemplate, KafkaTemplate<String, Map<UUID, String>> mapUUIDToStringKafkaTemplate, KafkaTemplate<String, Map<String, String>> mapStringToStringKafkaTemplate) {
        this.uuidKafkaTemplate = uuidKafkaTemplate;
        this.stringKafkaTemplate = stringKafkaTemplate;
        this.mapUUIDToStringKafkaTemplate = mapUUIDToStringKafkaTemplate;
        this.mapStringToStringKafkaTemplate = mapStringToStringKafkaTemplate;
    }


    @Override
    @KafkaListener(topics = "card-error", groupId = "api-gateway",
            containerFactory = "errorDTOKafkaListenerFactory")
    public void handleCardErrors(ErrorDTO cardErrorDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.error("Received error topic with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureErrorResponse = responseFutures.remove(correlationId);
        logger.info("Complete CompletableFuture exceptionally with message: {} ", cardErrorDTO.toString());
        futureErrorResponse.completeExceptionally(new ResponseStatusException(HttpStatus.valueOf(
                cardErrorDTO.getStatus()), cardErrorDTO.getMessage()));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> createCard(UUID accountId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: create-card-by-account-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("create-card-by-account-id", accountId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "create-card-by-account-id-response", groupId = "api-gateway",
            containerFactory = "cardDTOKafkaListenerFactory")
    public void handleCardCreationResponse(CardDTO cardDTO,
                                           @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: create-card-by-account-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", cardDTO);
        futureResponse.complete(ResponseEntity.ok(cardDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getCardById(UUID cardId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-card-by-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("get-card-by-id", cardId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-card-by-id-response", groupId = "api-gateway",
            containerFactory = "cardDTOKafkaListenerFactory")
    public void handleGetCardByIdResponse(CardDTO cardDTO,
                                          @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-card-by-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", cardDTO);
        futureResponse.complete(ResponseEntity.ok(cardDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> getCardByCardNumber(String cardNumber) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-card-by-card-number with correlation id: {} ", correlationId);
        ProducerRecord<String, String> topic = new ProducerRecord<>("get-card-by-card-number", cardNumber);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        stringKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-card-by-card-number-response", groupId = "api-gateway",
            containerFactory = "cardDTOKafkaListenerFactory")
    public void handleGetCardByNumberResponse(CardDTO cardDTO,
                                              @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-card-by-card-number with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", cardDTO);
        futureResponse.complete(ResponseEntity.ok(cardDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getCardsByCardHolderFullName(String cardHolderFullName) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-all-cards-by-holder-name with correlation id: {} ", correlationId);
        ProducerRecord<String, String> topic = new ProducerRecord<>(
                "get-all-cards-by-holder-name", cardHolderFullName);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        stringKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntitysCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-all-cards-by-holder-name-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllCardByHolderFullNameResponse(List<CardDTO> cardDTOS,
                                                         @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-all-cards-by-holder-name with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = responseListFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", cardDTOS);
        futureResponse.complete(ResponseEntity.ok(cardDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllAccountCardsByAccountId(UUID accountId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-all-cards-by-account-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("get-all-cards-by-account-id", accountId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntitysCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-all-cards-by-account-id-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllCardByAccountIdResponse(List<CardDTO> cardDTOS,
                                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-all-cards-by-account-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = responseListFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", cardDTOS);
        futureResponse.complete(ResponseEntity.ok(cardDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllUserCardsByCardHolderId(UUID holderId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-all-cards-by-holder-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("get-all-cards-by-holder-id", holderId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntitysCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-all-cards-by-holder-id-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllCardByHolderIdResponse(List<CardDTO> cardDTOS,
                                                   @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-all-cards-by-holder-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = responseListFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", cardDTOS);
        futureResponse.complete(ResponseEntity.ok(cardDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllUserCardsByStatus(UUID holderId, String status) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: update-card-status-by-id with correlation id: {} ", correlationId);
        Map<UUID, String> getCardsRequestMap = Map.of(holderId, status);
        ProducerRecord<String, Map<UUID, String>> topic = new ProducerRecord<>("update-card-status-by-id",
                getCardsRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        mapUUIDToStringKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntitysCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "update-card-status-by-id-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllCardByStatusNameResponse(List<CardDTO> cardDTOS,
                                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: update-card-status-by-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = responseListFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", cardDTOS);
        futureResponse.complete(ResponseEntity.ok(cardDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllExpiredCards(UUID holderId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-all-expired-cards-by-holder-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("get-all-expired-cards-by-holder-id", holderId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntitysCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-all-expired-cards-by-holder-id-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllExpiredCardsResponse(List<CardDTO> cardDTOS,
                                                 @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-all-expired-cards-by-holder-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = responseListFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", cardDTOS);
        futureResponse.complete(ResponseEntity.ok(cardDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Object>>> getAllActiveCards(UUID holderId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = new CompletableFuture<>();
        responseListFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: get-all-active-cards-by-holder-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("get-all-active-cards-by-holder-id", holderId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntitysCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "get-all-active-cards-by-holder-id-response", groupId = "api-gateway",
            containerFactory = "listKafkaListenerFactory")
    public void handleGetAllActiveCardsResponse(List<CardDTO> cardDTOS,
                                                @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: get-all-active-cards-by-holder-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse = responseListFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", cardDTOS);
        futureResponse.complete(ResponseEntity.ok(cardDTOS));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateCardStatusById(UUID cardId, String status) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: update-card-status-by-id with correlation id: {} ", correlationId);
        Map<UUID, String> updateCardRequestMap = Map.of(cardId, status);
        ProducerRecord<String, Map<UUID, String>> topic = new ProducerRecord<>("update-card-status-by-id",
                updateCardRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        mapUUIDToStringKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "update-card-status-by-id-response", groupId = "api-gateway",
            containerFactory = "cardDTOKafkaListenerFactory")
    public void handleUpdateCardStatusByIdResponse(CardDTO cardDTO,
                                                   @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: update-card-status-by-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", cardDTO);
        futureResponse.complete(ResponseEntity.ok(cardDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> updateCardStatusByCardNumber(String cardNumber, String status) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: update-card-status-by-card-number with correlation id: {} ", correlationId);
        Map<String, String> updateCardRequestMap = Map.of(cardNumber, status);
        ProducerRecord<String, Map<String, String>> topic = new ProducerRecord<>("update-card-status-by-card-number",
                updateCardRequestMap);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        mapStringToStringKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "update-card-status-by-card-number-response", groupId = "api-gateway",
            containerFactory = "cardDTOKafkaListenerFactory")
    public void handleUpdateCardStatusByNumberResponse(CardDTO cardDTO,
                                                       @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: update-card-status-by-card-number with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", cardDTO);
        futureResponse.complete(ResponseEntity.ok(cardDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteCardById(UUID cardId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: delete-card-by-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("delete-card-by-id", cardId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "delete-card-by-id-response", groupId = "api-gateway",
            containerFactory = "stringKafkaListenerFactory")
    public void handleDeleteCardByCardIdResponse(CardDTO cardDTO,
                                                 @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: delete-card-by-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> responseMessage = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", responseMessage);
        responseMessage.complete(ResponseEntity.ok(cardDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteAllAccountCardsByAccountId(UUID accountId) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: delete-card-by-account-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("delete-card-by-account-id", accountId);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "delete-card-by-account-id-response", groupId = "api-gateway",
            containerFactory = "stringKafkaListenerFactory")
    public void handleDeleteCardByAccountIdResponse(CardDTO cardDTO,
                                                    @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: delete-card-by-account-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> responseMessage = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", responseMessage);
        responseMessage.complete(ResponseEntity.ok(cardDTO));
    }

    @Override
    public CompletableFuture<ResponseEntity<Object>> deleteAllUsersCardsByCardHolderUUID(UUID cardHolderUUID) {
        String correlationId = UUID.randomUUID().toString();
        logger.debug("Creating expected future result with correlation id: {} ", correlationId);
        CompletableFuture<ResponseEntity<Object>> futureResponse = new CompletableFuture<>();
        responseFutures.put(correlationId, futureResponse);

        logger.info("Trying to create topic: delete-card-by-holder-id with correlation id: {} ", correlationId);
        ProducerRecord<String, UUID> topic = new ProducerRecord<>("delete-card-by-holder-id", cardHolderUUID);
        topic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        uuidKafkaTemplate.send(topic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", topic.value());
        return getResponseEntityCompletableFuture(futureResponse);
    }

    @Override
    @KafkaListener(topics = "delete-card-by-holder-id-response", groupId = "api-gateway",
            containerFactory = "stringKafkaListenerFactory")
    public void handleDeleteAllCardsByHolderIdResponse(CardDTO cardDTO,
                                                       @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Response from topic: delete-card-by-holder-id with correlation id: {} " +
                "was received successfully", correlationId);
        CompletableFuture<ResponseEntity<Object>> responseMessage = responseFutures.remove(correlationId);
        logger.debug("Future expectation with correlation id: {} was removed from expectations", correlationId);
        logger.info("Completing expected future response with: {}", responseMessage);
        responseMessage.complete(ResponseEntity.ok(cardDTO));
    }

    private CompletableFuture<ResponseEntity<List<Object>>> getResponseEntitysCompletableFuture(
            CompletableFuture<ResponseEntity<List<CardDTO>>> futureResponse) {
        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null) {
                        logger.info("Request successfully collapsed and received to the Controller");
                        return ResponseEntity.ok((List<Object>) response);
                    } else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT,
                                "Request timed out, service unreachable, please try again later");
                    }
                });
    }

    private CompletableFuture<ResponseEntity<Object>> getResponseEntityCompletableFuture(
            CompletableFuture<ResponseEntity<Object>> futureResponse) {
        return futureResponse.completeOnTimeout(null, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(response -> {
                    if (response != null) {
                        logger.info("Request successfully collapsed and received to the Controller");
                        return ResponseEntity.ok(response.getBody());
                    } else {
                        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT,
                                "Request timed out, service unreachable, please try again later");
                    }
                });
    }
}
