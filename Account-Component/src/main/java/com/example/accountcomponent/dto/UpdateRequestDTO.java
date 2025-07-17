package com.example.accountcomponent.dto;

import java.util.UUID;

public record UpdateRequestDTO(UUID accountId, AccountDTO accountDTO) {
}
