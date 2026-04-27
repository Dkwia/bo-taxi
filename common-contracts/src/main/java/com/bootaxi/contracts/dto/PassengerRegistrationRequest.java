package com.bootaxi.contracts.dto;

public record PassengerRegistrationRequest(
        String name,
        String email,
        String phone,
        String password
) {
}
