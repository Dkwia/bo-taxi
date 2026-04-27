package com.bootaxi.contracts.dto;

import java.time.Instant;

public record PassengerResponse(
        Long id,
        String name,
        String email,
        String phone,
        Instant createdAt
) {
}
