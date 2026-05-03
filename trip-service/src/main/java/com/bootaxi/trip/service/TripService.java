package com.bootaxi.trip.service;

import com.bootaxi.contracts.amqp.DriverReservationReply;
import com.bootaxi.contracts.amqp.TripStatusChangedEvent;
import com.bootaxi.contracts.dto.DailyStatisticsResponse;
import com.bootaxi.contracts.dto.TripCreateRequest;
import com.bootaxi.contracts.dto.TripRatingRequest;
import com.bootaxi.contracts.dto.TripResponse;
import com.bootaxi.contracts.dto.TripStatusUpdateRequest;
import com.bootaxi.contracts.enums.DriverStatus;
import com.bootaxi.contracts.enums.TripStatus;
import com.bootaxi.contracts.enums.UserRole;
import com.bootaxi.contracts.security.JwtClaims;
import com.bootaxi.trip.amqp.TripEventPublisher;
import com.bootaxi.trip.amqp.UserCommandClient;
import com.bootaxi.trip.domain.Trip;
import com.bootaxi.trip.exception.ConflictException;
import com.bootaxi.trip.exception.ForbiddenException;
import com.bootaxi.trip.exception.NotFoundException;
import com.bootaxi.trip.repository.TripRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final UserCommandClient userCommandClient;
    private final TripEventPublisher tripEventPublisher;
    private final DistanceCalculator distanceCalculator;
    private final BigDecimal farePerKilometer;

    public TripService(TripRepository tripRepository,
                       UserCommandClient userCommandClient,
                       TripEventPublisher tripEventPublisher,
                       DistanceCalculator distanceCalculator,
                       @Value("${app.pricing.fare-per-km:2.50}") BigDecimal farePerKilometer) {
        this.tripRepository = tripRepository;
        this.userCommandClient = userCommandClient;
        this.tripEventPublisher = tripEventPublisher;
        this.distanceCalculator = distanceCalculator;
        this.farePerKilometer = farePerKilometer;
    }

    @Transactional
    public TripResponse createTrip(TripCreateRequest request, JwtClaims claims) {
        if (claims.role() != UserRole.PASSENGER) {
            throw new ForbiddenException("Only passengers can create trips");
        }
        Long passengerId = resolvePassengerId(request.passengerId(), claims);
        ensurePassengerAccess(passengerId, claims);

        if (!userCommandClient.passengerExists(passengerId)) {
            throw new NotFoundException("Passenger " + passengerId + " not found");
        }

        DriverReservationReply reservation = userCommandClient.reserveDriver();
        if (!reservation.reserved() || reservation.driverId() == null) {
            throw new ConflictException("No available drivers were found");
        }

        try {
            Trip trip = new Trip();
            trip.setPassengerId(passengerId);
            trip.setDriverId(reservation.driverId());
            trip.setOrigin(requireText(request.origin(), "Origin is required"));
            trip.setDestination(requireText(request.destination(), "Destination is required"));
            trip.setDistanceKm(distanceCalculator.resolveDistance(request.origin(), request.destination(), request.distanceKm()));
            trip.setPrice(calculatePrice(trip.getDistanceKm()));
            trip.setStatus(TripStatus.DRIVER_ASSIGNED);

            Trip saved = tripRepository.save(trip);
            publishTripEvent(saved, "Driver assigned");
            return toResponse(saved);
        } catch (RuntimeException exception) {
            userCommandClient.updateDriverStatus(reservation.driverId(), DriverStatus.AVAILABLE);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public TripResponse getTrip(Long tripId, JwtClaims claims) {
        Trip trip = findTrip(tripId);
        ensureTripAccess(trip, claims);
        return toResponse(trip);
    }

    @Transactional(readOnly = true)
    public List<TripResponse> tripHistory(Long passengerId, JwtClaims claims) {
        ensurePassengerAccess(passengerId, claims);
        return tripRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId).stream()
                .map(TripService::toResponse)
                .toList();
    }

    @Transactional
    public TripResponse updateStatus(Long tripId, TripStatusUpdateRequest request, JwtClaims claims) {
        Trip trip = findTrip(tripId);
        ensureDriverAccess(trip, claims);
        validateTransition(trip.getStatus(), request.status());

        if (request.status() == TripStatus.DRIVER_ACCEPTED) {
            userCommandClient.updateDriverStatus(trip.getDriverId(), DriverStatus.BUSY);
        } else if (request.status() == TripStatus.COMPLETED || request.status() == TripStatus.CANCELLED) {
            userCommandClient.updateDriverStatus(trip.getDriverId(), DriverStatus.AVAILABLE);
        }

        trip.setStatus(request.status());
        Trip saved = tripRepository.save(trip);
        publishTripEvent(saved, "Trip status changed to " + saved.getStatus().name());
        return toResponse(saved);
    }

    @Transactional
    public TripResponse rateTrip(Long tripId, TripRatingRequest request, JwtClaims claims) {
        Trip trip = findTrip(tripId);
        ensurePassengerAccess(trip.getPassengerId(), claims);

        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new ConflictException("Only completed trips can be rated");
        }
        if (request.rating() < 1 || request.rating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (trip.getRating() != null) {
            throw new ConflictException("Trip " + tripId + " already has a rating");
        }

        trip.setRating(request.rating());
        Trip saved = tripRepository.save(trip);
        publishTripEvent(saved, "Passenger rated the trip");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DailyStatisticsResponse dailyStatistics(LocalDate date) {
        LocalDate effectiveDate = date == null ? LocalDate.now(ZoneOffset.UTC) : date;
        Instant start = effectiveDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = effectiveDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Object[] raw = tripRepository.dailyStatistics(start, end);
        long tripsCount = raw[0] == null ? 0L : ((Number) raw[0]).longValue();
        BigDecimal averagePrice = raw[1] == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(((Number) raw[1]).doubleValue()).setScale(2, RoundingMode.HALF_UP);
        return new DailyStatisticsResponse(effectiveDate, tripsCount, averagePrice);
    }

    private void validateTransition(TripStatus currentStatus, TripStatus requestedStatus) {
        boolean valid = switch (currentStatus) {
            case DRIVER_ASSIGNED -> requestedStatus == TripStatus.DRIVER_ACCEPTED || requestedStatus == TripStatus.CANCELLED;
            case DRIVER_ACCEPTED -> requestedStatus == TripStatus.IN_PROGRESS || requestedStatus == TripStatus.CANCELLED;
            case IN_PROGRESS -> requestedStatus == TripStatus.COMPLETED || requestedStatus == TripStatus.CANCELLED;
            default -> false;
        };

        if (!valid) {
            throw new ConflictException("Invalid transition from " + currentStatus + " to " + requestedStatus);
        }
    }

    private void publishTripEvent(Trip trip, String message) {
        tripEventPublisher.publish(new TripStatusChangedEvent(
                trip.getId(),
                trip.getPassengerId(),
                trip.getDriverId(),
                trip.getStatus(),
                trip.getPrice(),
                message,
                Instant.now()
        ));
    }

    private BigDecimal calculatePrice(BigDecimal distanceKm) {
        return distanceKm.multiply(farePerKilometer).setScale(2, RoundingMode.HALF_UP);
    }

    private Trip findTrip(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip " + tripId + " not found"));
    }

    private void ensureTripAccess(Trip trip, JwtClaims claims) {
        if (claims.role() == UserRole.PASSENGER && claims.userId().equals(trip.getPassengerId())) {
            return;
        }
        if (claims.role() == UserRole.DRIVER && claims.userId().equals(trip.getDriverId())) {
            return;
        }
        throw new ForbiddenException("Access is denied");
    }

    private void ensurePassengerAccess(Long passengerId, JwtClaims claims) {
        if (claims == null || claims.role() != UserRole.PASSENGER || claims.userId() == null || !claims.userId().equals(passengerId)) {
            throw new ForbiddenException("Passenger access is denied");
        }
    }

    private void ensureDriverAccess(Trip trip, JwtClaims claims) {
        if (claims == null || claims.role() != UserRole.DRIVER || claims.userId() == null || !claims.userId().equals(trip.getDriverId())) {
            throw new ForbiddenException("Driver access is denied");
        }
    }

    private Long resolvePassengerId(Long passengerIdFromRequest, JwtClaims claims) {
        return passengerIdFromRequest != null ? passengerIdFromRequest : claims.userId();
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    static TripResponse toResponse(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getPassengerId(),
                trip.getDriverId(),
                trip.getStatus(),
                trip.getOrigin(),
                trip.getDestination(),
                trip.getDistanceKm(),
                trip.getPrice(),
                trip.getRating(),
                trip.getCreatedAt(),
                trip.getUpdatedAt()
        );
    }
}
