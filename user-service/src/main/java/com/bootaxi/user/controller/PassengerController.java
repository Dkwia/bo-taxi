package com.bootaxi.user.controller;

import com.bootaxi.contracts.dto.PassengerRegistrationRequest;
import com.bootaxi.contracts.dto.PassengerResponse;
import com.bootaxi.contracts.enums.UserRole;
import com.bootaxi.user.security.SecurityUtils;
import com.bootaxi.user.service.PassengerService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/passengers")
public class PassengerController {

    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PassengerResponse register(@RequestBody PassengerRegistrationRequest request) {
        return passengerService.register(request);
    }

    @GetMapping("/{id}")
    public PassengerResponse getById(@PathVariable Long id, Authentication authentication) {
        SecurityUtils.requireSameUserOrInternal(authentication, id, UserRole.PASSENGER);
        return passengerService.getById(id);
    }
}
