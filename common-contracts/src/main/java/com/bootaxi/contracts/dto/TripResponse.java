package com.bootaxi.contracts.dto;

import com.bootaxi.contracts.enums.TripStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TripResponse(
        Long id,
        Long passengerId,
        Long driverId,
        TripStatus status,
        String origin,
        String destination,
        BigDecimal distanceKm,
        BigDecimal price,
        Integer rating,
        Instant createdAt,
        Instant updatedAt
) {
}
