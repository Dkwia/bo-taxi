package com.bootaxi.contracts.dto;

import com.bootaxi.contracts.enums.DriverStatus;

public record DriverStatusUpdateRequest(
        DriverStatus status
) {
}
