package com.bootaxi.user.controller;

import com.bootaxi.contracts.dto.DriverRegistrationRequest;
import com.bootaxi.contracts.dto.DriverResponse;
import com.bootaxi.contracts.dto.DriverStatusUpdateRequest;
import com.bootaxi.contracts.enums.UserRole;
import com.bootaxi.user.security.SecurityUtils;
import com.bootaxi.user.service.DriverService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DriverResponse register(@RequestBody DriverRegistrationRequest request) {
        return driverService.register(request);
    }

    @GetMapping("/{id}")
    public DriverResponse getById(@PathVariable Long id, Authentication authentication) {
        SecurityUtils.requireSameUserOrInternal(authentication, id, UserRole.DRIVER);
        return driverService.getById(id);
    }

    @PatchMapping("/{id}/status")
    public DriverResponse updateStatus(@PathVariable Long id,
                                       @RequestBody DriverStatusUpdateRequest request,
                                       Authentication authentication) {
        SecurityUtils.requireSameUserOrInternal(authentication, id, UserRole.DRIVER);
        return driverService.updateStatus(id, request.status());
    }
}
