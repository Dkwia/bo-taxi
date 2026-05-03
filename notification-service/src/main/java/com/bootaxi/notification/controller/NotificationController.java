package com.bootaxi.notification.controller;

import com.bootaxi.contracts.dto.NotificationCreateRequest;
import com.bootaxi.contracts.dto.NotificationResponse;
import com.bootaxi.notification.service.NotificationTaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationTaskService notificationTaskService;

    public NotificationController(NotificationTaskService notificationTaskService) {
        this.notificationTaskService = notificationTaskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(@RequestBody NotificationCreateRequest request) {
        return notificationTaskService.create(request);
    }

    @GetMapping
    public List<NotificationResponse> byTrip(@RequestParam("trip_id") Long tripId) {
        return notificationTaskService.findByTripId(tripId);
    }
}
