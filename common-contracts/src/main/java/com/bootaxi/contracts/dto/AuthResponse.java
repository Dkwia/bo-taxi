package com.bootaxi.contracts.dto;

import java.time.Instant;

public record AuthResponse(
        String token,
        Instant expiresAt
) {
}
