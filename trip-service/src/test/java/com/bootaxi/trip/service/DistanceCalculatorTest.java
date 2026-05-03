package com.bootaxi.trip.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DistanceCalculatorTest {

    private final DistanceCalculator distanceCalculator = new DistanceCalculator();

    @Test
    void shouldReturnRoundedProvidedDistance() {
        BigDecimal distance = distanceCalculator.resolveDistance("ignored", "ignored", new BigDecimal("12.345"));

        assertEquals(new BigDecimal("12.35"), distance);
    }

    @Test
    void shouldCalculateDistanceFromCoordinates() {
        BigDecimal distance = distanceCalculator.resolveDistance("0,0", "0,1", null);

        assertEquals(new BigDecimal("111.19"), distance);
    }

    @Test
    void shouldRejectInvalidProvidedDistance() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> distanceCalculator.resolveDistance("0,0", "0,1", BigDecimal.ZERO));

        assertEquals("Distance must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldRejectInvalidCoordinateFormat() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> distanceCalculator.resolveDistance("invalid", "0,1", null));

        assertEquals("Location must be 'latitude,longitude' or distanceKm must be provided", exception.getMessage());
    }
}
