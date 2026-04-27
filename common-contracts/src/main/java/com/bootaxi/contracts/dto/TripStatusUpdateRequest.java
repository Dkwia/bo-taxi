package com.bootaxi.contracts.dto;

import com.bootaxi.contracts.enums.TripStatus;

public record TripStatusUpdateRequest(
        TripStatus status
) {
}
