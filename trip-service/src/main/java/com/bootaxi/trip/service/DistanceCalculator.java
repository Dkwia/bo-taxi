package com.bootaxi.trip.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DistanceCalculator {

    public BigDecimal resolveDistance(String origin, String destination, BigDecimal providedDistanceKm) {
        if (providedDistanceKm != null) {
            if (providedDistanceKm.signum() <= 0) {
                throw new IllegalArgumentException("Distance must be greater than zero");
            }
            return providedDistanceKm.setScale(2, RoundingMode.HALF_UP);
        }

        Point originPoint = parsePoint(origin);
        Point destinationPoint = parsePoint(destination);
        return haversine(originPoint, destinationPoint).setScale(2, RoundingMode.HALF_UP);
    }

    private Point parsePoint(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Location must not be empty");
        }

        String[] parts = value.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Location must be 'latitude,longitude' or distanceKm must be provided");
        }

        try {
            return new Point(Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim()));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Location must be 'latitude,longitude' or distanceKm must be provided");
        }
    }

    private BigDecimal haversine(Point origin, Point destination) {
        double earthRadiusKm = 6371.0;
        double latDistance = Math.toRadians(destination.latitude() - origin.latitude());
        double lonDistance = Math.toRadians(destination.longitude() - origin.longitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(origin.latitude()))
                * Math.cos(Math.toRadians(destination.latitude()))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(earthRadiusKm * c);
    }

    private record Point(double latitude, double longitude) {
    }
}
