package com.bootaxi.contracts.amqp;

import com.bootaxi.contracts.enums.TripStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TripStatusChangedEvent(
        Long tripId,
        Long passengerId,
        Long driverId,
        TripStatus status,
        BigDecimal price,
        String message,
        Instant occurredAt
) {
}
