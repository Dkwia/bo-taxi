package com.bootaxi.contracts.dto;

import com.bootaxi.contracts.enums.NotificationStatus;
import com.bootaxi.contracts.enums.RecipientType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long tripId,
        RecipientType recipientType,
        Long recipientId,
        String message,
        NotificationStatus status,
        int attempts,
        Instant createdAt,
        Instant updatedAt
) {
}
