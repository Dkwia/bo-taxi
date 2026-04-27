package com.bootaxi.contracts.amqp;

import com.bootaxi.contracts.enums.DriverStatus;

public record DriverStatusCommand(
        Long driverId,
        DriverStatus status
) {
}
