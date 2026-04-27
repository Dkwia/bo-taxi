package com.bootaxi.contracts.amqp;

import com.bootaxi.contracts.enums.DriverStatus;

public record DriverStatusReply(
        Long driverId,
        DriverStatus status,
        boolean updated
) {
}
