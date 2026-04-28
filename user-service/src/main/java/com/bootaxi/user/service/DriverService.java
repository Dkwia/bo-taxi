package com.bootaxi.user.service;

import com.bootaxi.contracts.amqp.DriverReservationReply;
import com.bootaxi.contracts.dto.DriverRegistrationRequest;
import com.bootaxi.contracts.dto.DriverResponse;
import com.bootaxi.contracts.enums.DriverStatus;
import com.bootaxi.user.domain.Driver;
import com.bootaxi.user.exception.ConflictException;
import com.bootaxi.user.exception.NotFoundException;
import com.bootaxi.user.repository.DriverRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final AvailableDriverCacheService availableDriverCacheService;

    public DriverService(DriverRepository driverRepository,
                         PasswordEncoder passwordEncoder,
                         AvailableDriverCacheService availableDriverCacheService) {
        this.driverRepository = driverRepository;
        this.passwordEncoder = passwordEncoder;
        this.availableDriverCacheService = availableDriverCacheService;
    }

    @Transactional
    public DriverResponse register(DriverRegistrationRequest request) {
        validateText(request.name(), "Driver name is required");
        validateText(request.email(), "Driver email is required");
        validateText(request.phone(), "Driver phone is required");
        validateText(request.password(), "Driver password is required");
        validateText(request.licenseNumber(), "Driver license number is required");

        if (driverRepository.existsByEmail(request.email())) {
            throw new ConflictException("Driver with email " + request.email() + " already exists");
        }
        if (driverRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw new ConflictException("Driver with license " + request.licenseNumber() + " already exists");
        }

        Driver driver = new Driver();
        driver.setName(request.name());
        driver.setEmail(request.email());
        driver.setPhone(request.phone());
        driver.setPasswordHash(passwordEncoder.encode(request.password()));
        driver.setLicenseNumber(request.licenseNumber());
        driver.setStatus(DriverStatus.AVAILABLE);

        Driver saved = driverRepository.save(driver);
        availableDriverCacheService.evict();
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DriverResponse getById(Long id) {
        return driverRepository.findById(id)
                .map(DriverService::toResponse)
                .orElseThrow(() -> new NotFoundException("Driver " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public Driver getByEmail(String email) {
        return driverRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Driver with email " + email + " not found"));
    }

    @Transactional
    public DriverResponse updateStatus(Long driverId, DriverStatus status) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new NotFoundException("Driver " + driverId + " not found"));
        driver.setStatus(status);
        Driver saved = driverRepository.save(driver);
        availableDriverCacheService.evict();
        return toResponse(saved);
    }

    @Transactional
    public DriverReservationReply reserveAvailableDriver() {
        Driver claimed = tryClaim(availableDriverCacheService.read());
        if (claimed != null) {
            return toReservationReply(claimed);
        }

        List<Long> freshDriverIds = driverRepository.findIdsByStatus(
                DriverStatus.AVAILABLE,
                availableDriverCacheService.page()
        );
        availableDriverCacheService.write(freshDriverIds);

        claimed = tryClaim(freshDriverIds);
        if (claimed == null) {
            return new DriverReservationReply(null, null, null, null, null, false);
        }
        return toReservationReply(claimed);
    }

    @Transactional
    public boolean updateStatusFromCommand(Long driverId, DriverStatus status) {
        int updated = driverRepository.updateStatus(driverId, status);
        availableDriverCacheService.evict();
        return updated > 0;
    }

    private Driver tryClaim(List<Long> driverIds) {
        for (Long driverId : driverIds) {
            int updated = driverRepository.updateStatusIfCurrent(driverId, DriverStatus.AVAILABLE, DriverStatus.RESERVED);
            if (updated > 0) {
                availableDriverCacheService.evict();
                return driverRepository.findById(driverId)
                        .orElseThrow(() -> new NotFoundException("Driver " + driverId + " not found after reservation"));
            }
        }
        return null;
    }

    static DriverResponse toResponse(Driver driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getName(),
                driver.getEmail(),
                driver.getPhone(),
                driver.getLicenseNumber(),
                driver.getStatus(),
                driver.getCreatedAt()
        );
    }

    private DriverReservationReply toReservationReply(Driver driver) {
        return new DriverReservationReply(
                driver.getId(),
                driver.getName(),
                driver.getEmail(),
                driver.getPhone(),
                driver.getLicenseNumber(),
                true
        );
    }

    private void validateText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
