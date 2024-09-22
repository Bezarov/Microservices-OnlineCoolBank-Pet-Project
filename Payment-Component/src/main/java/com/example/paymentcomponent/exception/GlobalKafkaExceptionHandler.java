package com.example.paymentcomponent.exception;

import com.example.paymentcomponent.dto.ErrorDTO;
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
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class GlobalKafkaExceptionHandler implements CommonErrorHandler {
    private final static Logger logger = LoggerFactory.getLogger(GlobalKafkaExceptionHandler.class);
    private final KafkaTemplate<String, ErrorDTO> paymentDTOErrorKafkaTemplate;

    public GlobalKafkaExceptionHandler(KafkaTemplate<String, ErrorDTO> paymentDTOErrorKafkaTemplate) {
        this.paymentDTOErrorKafkaTemplate = paymentDTOErrorKafkaTemplate;
    }


    @Override
    public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record, Consumer<?, ?> consumer,
                             MessageListenerContainer container) {
        //Catch Exceptions and call handleRemaining
        handleRemaining(thrownException, List.of(record), consumer, container);
        return true;
    }

    @Override
    public void handleRemaining(Exception thrownException, List<ConsumerRecord<?, ?>> records,
                                Consumer<?, ?> consumer, MessageListenerContainer container) {
        //Check if exception is type ListenerExecutionFailedException than get cause, if not save it as is
        Throwable cause = (thrownException instanceof ListenerExecutionFailedException)
                ? thrownException.getCause() : thrownException;

        //Cast Throwable-cause to ResponseStatusException and call KafkaErrorProducer
        ResponseStatusException responseStatusException = (ResponseStatusException) cause;
        KafkaErrorProducer(responseStatusException);
    }

    public void KafkaErrorProducer(ResponseStatusException exception) {
        //Extract from exception - correlation id and exception reason
        String correlationId = exception.getMessage().replaceAll("^.*correlationId:|[\"\\s]", "").trim();
        String exceptionReason = exception.getReason().replaceAll("correlationId:(.*)$", "").trim();

        //Create and fill in ErrorDTO
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setStatus(exception.getStatusCode().value());
        errorDTO.setMessage(exceptionReason);

        logger.info("Trying to create topic: payment-error with correlation id: {} ", correlationId);
        ProducerRecord<String, ErrorDTO> errorTopic = new ProducerRecord<>("payment-error", null, errorDTO);
        errorTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        paymentDTOErrorKafkaTemplate.send(errorTopic);
        logger.info("Error topic was created and allocated in kafka broker successfully: {}", errorTopic.value());
    }
}
