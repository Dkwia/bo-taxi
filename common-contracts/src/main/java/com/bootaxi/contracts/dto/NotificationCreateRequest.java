package com.bootaxi.contracts.dto;

import com.bootaxi.contracts.enums.RecipientType;

public record NotificationCreateRequest(
        Long tripId,
        RecipientType recipientType,
        Long recipientId,
        String message
) {
}
