package com.example.apigatewaycomponent.exception;

import com.example.apigatewaycomponent.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@ControllerAdvice
public class GlobalRestExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDTO> handleResponseStatusException(ResponseStatusException exception,
                                                                          HttpServletRequest request) {
        LOGGER.info("Response Exception was intercepted and received, Status code: {}, Reason: {} ",
                exception.getStatusCode(), exception.getReason());

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                exception.getStatusCode().toString(),
                exception.getReason(),
                request.getRequestURI()
        );

        return ResponseEntity.status(exception.getStatusCode()).body(errorResponse);
    }
}
