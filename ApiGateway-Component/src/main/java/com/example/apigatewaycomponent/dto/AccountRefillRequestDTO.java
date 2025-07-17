package com.example.apigatewaycomponent.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountRefillRequestDTO(UUID accountId, BigDecimal amount) {
}
