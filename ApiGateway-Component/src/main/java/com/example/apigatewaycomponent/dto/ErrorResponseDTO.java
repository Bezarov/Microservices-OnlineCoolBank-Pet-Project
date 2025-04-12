package com.example.apigatewaycomponent.dto;

import java.time.LocalDateTime;

public record ErrorResponseDTO(LocalDateTime timestamp, String error, String message, String path) {
}
