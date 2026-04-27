package com.bootaxi.contracts.dto;

import com.bootaxi.contracts.enums.UserRole;

public record AuthRequest(
        String email,
        String password,
        UserRole role
) {
}
