package com.bootaxi.trip.security;

import com.bootaxi.contracts.security.JwtClaims;
import com.bootaxi.trip.exception.ForbiddenException;
import org.springframework.security.core.Authentication;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static JwtClaims claims(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtClaims claims)) {
            throw new ForbiddenException("Authentication is required");
        }
        return claims;
    }
}
