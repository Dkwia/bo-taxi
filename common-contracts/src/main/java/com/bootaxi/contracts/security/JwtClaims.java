package com.bootaxi.contracts.security;

import com.bootaxi.contracts.enums.UserRole;

public record JwtClaims(
        Long userId,
        String email,
        UserRole role
) {
}
