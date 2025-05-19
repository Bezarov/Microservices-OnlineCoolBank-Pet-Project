package com.example.paymentcomponent.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DateRangeRequestDTO(UUID accountId, LocalDateTime fromDate, LocalDateTime toDate) {
}
