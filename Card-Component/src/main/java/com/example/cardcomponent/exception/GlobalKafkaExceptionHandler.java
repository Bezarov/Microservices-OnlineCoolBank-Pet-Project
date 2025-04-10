package com.example.cardcomponent.exception;

import com.example.cardcomponent.dto.ErrorDTO;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class GlobalKafkaExceptionHandler implements CommonErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalKafkaExceptionHandler.class);
    private final KafkaTemplate<String, ErrorDTO> cardDTOErrorKafkaTemplate;

    public GlobalKafkaExceptionHandler(KafkaTemplate<String, ErrorDTO> cardDTOErrorKafkaTemplate) {
        this.cardDTOErrorKafkaTemplate = cardDTOErrorKafkaTemplate;
    }

    @Override
    public boolean handleOne(@NonNull Exception thrownException, @NonNull ConsumerRecord<?, ?> rec,
                             @NonNull Consumer<?, ?> consumer, @NonNull MessageListenerContainer container) {
        handleRemaining(thrownException, List.of(rec), consumer, container);
        return true;
    }

    @Override
    public void handleRemaining(@NonNull Exception thrownException, @NonNull List<ConsumerRecord<?, ?>> records,
                                @NonNull Consumer<?, ?> consumer, @NonNull MessageListenerContainer container) {
        Throwable cause = (thrownException instanceof ListenerExecutionFailedException)
                ? thrownException.getCause()
                : thrownException;

        if (cause instanceof CustomKafkaException customKafkaException) {
            kafkaErrorProducer(customKafkaException);
        } else {
            LOGGER.error("Unexpected exception type in Kafka error handler", cause);
        }
    }

    public void kafkaErrorProducer(CustomKafkaException exception) {
        String correlationId = extractCorrelationId(exception.getReason());
        String exceptionReason = extractExceptionReason(exception.getReason());

        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setStatus(exception.getStatusCode().value());
        errorDTO.setMessage(exceptionReason);

        LOGGER.info("Create topic: card-error with correlation id: {} ", correlationId);
        ProducerRecord<String, ErrorDTO> errorTopic = new ProducerRecord<>("card-error", null, errorDTO);
        errorTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        cardDTOErrorKafkaTemplate.send(errorTopic);
        LOGGER.info("Error topic was created and allocated in kafka broker successfully: {}", errorTopic.value());
    }

    private String extractCorrelationId(String reason) {
        return Objects.requireNonNull(reason)
                .replaceAll("^.*correlationId:\\s*", "")
                .replaceAll("[\"\\s]", "")
                .trim();
    }

    private String extractExceptionReason(String reason) {
        return Objects.requireNonNull(reason)
                .replaceAll("correlationId:(.*)$", "")
                .trim();
    }
}
