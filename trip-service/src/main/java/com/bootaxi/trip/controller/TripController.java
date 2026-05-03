package com.bootaxi.trip.controller;

import com.bootaxi.contracts.dto.DailyStatisticsResponse;
import com.bootaxi.contracts.dto.TripCreateRequest;
import com.bootaxi.contracts.dto.TripRatingRequest;
import com.bootaxi.contracts.dto.TripResponse;
import com.bootaxi.contracts.dto.TripStatusUpdateRequest;
import com.bootaxi.contracts.security.JwtClaims;
import com.bootaxi.trip.security.SecurityUtils;
import com.bootaxi.trip.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponse createTrip(@RequestBody TripCreateRequest request, Authentication authentication) {
        JwtClaims claims = SecurityUtils.claims(authentication);
        return tripService.createTrip(request, claims);
    }

    @GetMapping("/{id}")
    public TripResponse getTrip(@PathVariable Long id, Authentication authentication) {
        return tripService.getTrip(id, SecurityUtils.claims(authentication));
    }

    @GetMapping
    public List<TripResponse> history(@RequestParam("passenger_id") Long passengerId, Authentication authentication) {
        return tripService.tripHistory(passengerId, SecurityUtils.claims(authentication));
    }

    @PatchMapping("/{id}/status")
    public TripResponse updateStatus(@PathVariable Long id,
                                     @RequestBody TripStatusUpdateRequest request,
                                     Authentication authentication) {
        return tripService.updateStatus(id, request, SecurityUtils.claims(authentication));
    }

    @PatchMapping("/{id}/rating")
    public TripResponse rateTrip(@PathVariable Long id,
                                 @RequestBody TripRatingRequest request,
                                 Authentication authentication) {
        return tripService.rateTrip(id, request, SecurityUtils.claims(authentication));
    }

    @GetMapping("/statistics/daily")
    public DailyStatisticsResponse statistics(@RequestParam(value = "date", required = false) LocalDate date) {
        return tripService.dailyStatistics(date);
    }
}
