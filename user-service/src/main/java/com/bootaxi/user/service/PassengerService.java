package com.bootaxi.user.service;

import com.bootaxi.contracts.dto.PassengerRegistrationRequest;
import com.bootaxi.contracts.dto.PassengerResponse;
import com.bootaxi.user.domain.Passenger;
import com.bootaxi.user.exception.ConflictException;
import com.bootaxi.user.exception.NotFoundException;
import com.bootaxi.user.repository.PassengerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PassengerService {

    private final PassengerRepository passengerRepository;
    private final PasswordEncoder passwordEncoder;

    public PassengerService(PassengerRepository passengerRepository, PasswordEncoder passwordEncoder) {
        this.passengerRepository = passengerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PassengerResponse register(PassengerRegistrationRequest request) {
        validateText(request.name(), "Passenger name is required");
        validateText(request.email(), "Passenger email is required");
        validateText(request.phone(), "Passenger phone is required");
        validateText(request.password(), "Passenger password is required");

        if (passengerRepository.existsByEmail(request.email())) {
            throw new ConflictException("Passenger with email " + request.email() + " already exists");
        }

        Passenger passenger = new Passenger();
        passenger.setName(request.name());
        passenger.setEmail(request.email());
        passenger.setPhone(request.phone());
        passenger.setPasswordHash(passwordEncoder.encode(request.password()));

        Passenger saved = passengerRepository.save(passenger);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PassengerResponse getById(Long id) {
        return passengerRepository.findById(id)
                .map(PassengerService::toResponse)
                .orElseThrow(() -> new NotFoundException("Passenger " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public Passenger getByEmail(String email) {
        return passengerRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Passenger with email " + email + " not found"));
    }

    @Transactional(readOnly = true)
    public boolean exists(Long passengerId) {
        return passengerRepository.existsById(passengerId);
    }

    static PassengerResponse toResponse(Passenger passenger) {
        return new PassengerResponse(
                passenger.getId(),
                passenger.getName(),
                passenger.getEmail(),
                passenger.getPhone(),
                passenger.getCreatedAt()
        );
    }

    private void validateText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
