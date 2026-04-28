package com.bootaxi.user.security;

import com.bootaxi.contracts.enums.UserRole;
import com.bootaxi.contracts.security.JwtClaims;
import com.bootaxi.user.exception.ForbiddenException;
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

    public static void requireSameUserOrInternal(Authentication authentication, Long userId, UserRole role) {
        JwtClaims claims = claims(authentication);
        if (claims.role() == UserRole.INTERNAL) {
            return;
        }
        if (claims.role() != role || !claims.userId().equals(userId)) {
            throw new ForbiddenException("Access is denied");
        }
    }
}
