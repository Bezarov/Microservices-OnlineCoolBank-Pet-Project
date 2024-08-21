package com.example.userscomponent.exception;

import com.example.userscomponent.dto.ErrorDTO;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class GlobalKafkaExceptionHandler implements CommonErrorHandler {

    private final KafkaTemplate<String, ErrorDTO> usersErrorKafkaTemplate;

    public GlobalKafkaExceptionHandler(KafkaTemplate<String, ErrorDTO> usersErrorKafkaTemplate) {
        this.usersErrorKafkaTemplate = usersErrorKafkaTemplate;
    }

    @Override
    public void handleRemaining(Exception thrownException, List<ConsumerRecord<?, ?>> records,
                                Consumer<?, ?> consumer, MessageListenerContainer container) {
        Throwable cause = (thrownException instanceof ListenerExecutionFailedException)
                ? thrownException.getCause() : thrownException;

        ResponseStatusException responseStatusException = (ResponseStatusException) cause;
        KafkaErrorProducer(responseStatusException);
    }

    public void KafkaErrorProducer(ResponseStatusException exception) {
        String correlationId = exception.getMessage().replaceAll("^.*correlationId:|[\"\\s]", "").trim();
        String exceptionReason = exception.getReason().replaceAll("correlationId:(.*)$", "").trim();

        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setStatus(exception.getStatusCode().value());
        errorDTO.setMessage(exceptionReason);
        errorDTO.setCorrelationId(correlationId);

        ProducerRecord<String, ErrorDTO> errorTopic = new ProducerRecord<>(
                "users-error", null, errorDTO);
        errorTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        usersErrorKafkaTemplate.send(errorTopic);
    }

    @Override
    public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record, Consumer<?, ?> consumer,
                             MessageListenerContainer container) {
        handleRemaining(thrownException, List.of(record), consumer, container);
        return true;
    }
}
