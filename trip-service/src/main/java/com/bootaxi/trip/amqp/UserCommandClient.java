package com.bootaxi.trip.amqp;

import com.bootaxi.contracts.amqp.DriverReservationReply;
import com.bootaxi.contracts.amqp.DriverStatusCommand;
import com.bootaxi.contracts.amqp.DriverStatusReply;
import com.bootaxi.contracts.amqp.MessagingTopology;
import com.bootaxi.contracts.amqp.PassengerExistsCommand;
import com.bootaxi.contracts.amqp.PassengerExistsReply;
import com.bootaxi.contracts.amqp.ReserveDriverCommand;
import com.bootaxi.contracts.enums.DriverStatus;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserCommandClient {

    private final RabbitTemplate rabbitTemplate;

    public UserCommandClient(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public boolean passengerExists(Long passengerId) {
        PassengerExistsReply reply = (PassengerExistsReply) rabbitTemplate.convertSendAndReceive(
                MessagingTopology.PASSENGER_EXISTS_QUEUE,
                new PassengerExistsCommand(passengerId)
        );
        return reply != null && reply.exists();
    }

    public DriverReservationReply reserveDriver() {
        DriverReservationReply reply = (DriverReservationReply) rabbitTemplate.convertSendAndReceive(
                MessagingTopology.DRIVER_RESERVE_QUEUE,
                new ReserveDriverCommand()
        );
        return reply == null ? new DriverReservationReply(null, null, null, null, null, false) : reply;
    }

    public boolean updateDriverStatus(Long driverId, DriverStatus status) {
        DriverStatusReply reply = (DriverStatusReply) rabbitTemplate.convertSendAndReceive(
                MessagingTopology.DRIVER_STATUS_QUEUE,
                new DriverStatusCommand(driverId, status)
        );
        return reply != null && reply.updated();
    }
}
