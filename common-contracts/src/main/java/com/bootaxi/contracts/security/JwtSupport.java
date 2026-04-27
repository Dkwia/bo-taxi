package com.bootaxi.contracts.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bootaxi.contracts.enums.UserRole;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public final class JwtSupport {

    private JwtSupport() {
    }

    public static String createToken(JwtClaims claims, String secret, String issuer, Duration ttl) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(ttl);

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(String.valueOf(claims.userId()))
                .withClaim("email", claims.email())
                .withClaim("role", claims.role().name())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiresAt))
                .sign(Algorithm.HMAC256(secret));
    }

    public static JwtClaims verify(String token, String secret, String issuer) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer(issuer)
                .build()
                .verify(token);

        return new JwtClaims(
                Long.parseLong(jwt.getSubject()),
                jwt.getClaim("email").asString(),
                UserRole.valueOf(jwt.getClaim("role").asString())
        );
    }
}
