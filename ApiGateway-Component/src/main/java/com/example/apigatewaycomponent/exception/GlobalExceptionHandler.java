package com.example.apigatewaycomponent.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException exception) {
        logger.info("Response Exception was intercepted and received, Status code: {}, Reason: {} ",
                exception.getStatusCode(), exception.getReason());
        return new ResponseEntity<>(exception.getReason(), exception.getStatusCode());
    }
}
