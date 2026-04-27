package com.bootaxi.contracts.amqp;

public record DriverReservationReply(
        Long driverId,
        String name,
        String email,
        String phone,
        String licenseNumber,
        boolean reserved
) {
}
