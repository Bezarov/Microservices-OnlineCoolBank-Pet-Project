package com.example.accountcomponent.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RefillRequestDTO(UUID accountId, BigDecimal amount) {
}
