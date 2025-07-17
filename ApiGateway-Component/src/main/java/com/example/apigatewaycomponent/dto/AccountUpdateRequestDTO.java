package com.example.apigatewaycomponent.dto;

import java.util.UUID;

public record AccountUpdateRequestDTO(UUID accountId, AccountDTO accountDTO) {
}
