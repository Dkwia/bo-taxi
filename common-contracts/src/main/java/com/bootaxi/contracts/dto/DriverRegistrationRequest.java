package com.bootaxi.contracts.dto;

public record DriverRegistrationRequest(
        String name,
        String email,
        String phone,
        String password,
        String licenseNumber
) {
}
