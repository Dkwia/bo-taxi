package com.bootaxi.contracts.amqp;

public record PassengerExistsCommand(
        Long passengerId
) {
}
