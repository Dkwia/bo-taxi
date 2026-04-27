package com.bootaxi.contracts.dto;

import com.bootaxi.contracts.enums.DriverStatus;

import java.time.Instant;

public record DriverResponse(
        Long id,
        String name,
        String email,
        String phone,
        String licenseNumber,
        DriverStatus status,
        Instant createdAt
) {
}
