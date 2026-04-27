package com.bootaxi.contracts.dto;

import java.math.BigDecimal;

public record TripCreateRequest(
        Long passengerId,
        String origin,
        String destination,
        BigDecimal distanceKm
) {
}
