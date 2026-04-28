package com.bootaxi.user.service;

import com.bootaxi.contracts.dto.AuthRequest;
import com.bootaxi.contracts.dto.AuthResponse;
import com.bootaxi.contracts.enums.UserRole;
import com.bootaxi.contracts.security.JwtClaims;
import com.bootaxi.user.domain.Driver;
import com.bootaxi.user.domain.Passenger;
import com.bootaxi.user.exception.UnauthorizedException;
import com.bootaxi.user.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {

    private final PassengerService passengerService;
    private final DriverService driverService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Duration tokenTtl;

    public AuthService(PassengerService passengerService,
                       DriverService driverService,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       @Value("${app.security.token-ttl-hours:8}") long tokenTtlHours) {
        this.passengerService = passengerService;
        this.driverService = driverService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenTtl = Duration.ofHours(tokenTtlHours);
    }

    public AuthResponse authenticate(AuthRequest request) {
        if (request.role() == null) {
            throw new UnauthorizedException("Role is required for authentication");
        }

        return switch (request.role()) {
            case PASSENGER -> issuePassengerToken(request);
            case DRIVER -> issueDriverToken(request);
            default -> throw new UnauthorizedException("Unsupported role " + request.role());
        };
    }

    private AuthResponse issuePassengerToken(AuthRequest request) {
        Passenger passenger = passengerService.getByEmail(request.email());
        ensurePasswordMatches(request.password(), passenger.getPasswordHash());
        Instant expiresAt = Instant.now().plus(tokenTtl);
        return new AuthResponse(
                jwtService.createToken(new JwtClaims(passenger.getId(), passenger.getEmail(), UserRole.PASSENGER), tokenTtl),
                expiresAt
        );
    }

    private AuthResponse issueDriverToken(AuthRequest request) {
        Driver driver = driverService.getByEmail(request.email());
        ensurePasswordMatches(request.password(), driver.getPasswordHash());
        Instant expiresAt = Instant.now().plus(tokenTtl);
        return new AuthResponse(
                jwtService.createToken(new JwtClaims(driver.getId(), driver.getEmail(), UserRole.DRIVER), tokenTtl),
                expiresAt
        );
    }

    private void ensurePasswordMatches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || rawPassword.isBlank() || !passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new UnauthorizedException("Invalid credentials");
        }
    }
}
