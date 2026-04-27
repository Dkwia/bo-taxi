package com.bootaxi.contracts.amqp;

public final class MessagingTopology {

    public static final String PASSENGER_EXISTS_QUEUE = "user.passenger.exists";
    public static final String DRIVER_RESERVE_QUEUE = "user.driver.reserve";
    public static final String DRIVER_STATUS_QUEUE = "user.driver.status";
    public static final String TRIP_EVENTS_EXCHANGE = "trip.events";
    public static final String TRIP_STATUS_QUEUE = "notification.trip.status";
    public static final String TRIP_STATUS_ROUTING_KEY = "trip.status.changed";

    private MessagingTopology() {
    }
}
