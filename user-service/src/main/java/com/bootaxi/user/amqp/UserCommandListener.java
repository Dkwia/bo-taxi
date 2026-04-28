package com.bootaxi.user.amqp;

import com.bootaxi.contracts.amqp.DriverReservationReply;
import com.bootaxi.contracts.amqp.DriverStatusCommand;
import com.bootaxi.contracts.amqp.DriverStatusReply;
import com.bootaxi.contracts.amqp.MessagingTopology;
import com.bootaxi.contracts.amqp.PassengerExistsCommand;
import com.bootaxi.contracts.amqp.PassengerExistsReply;
import com.bootaxi.contracts.amqp.ReserveDriverCommand;
import com.bootaxi.user.service.DriverService;
import com.bootaxi.user.service.PassengerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserCommandListener {

    private final PassengerService passengerService;
    private final DriverService driverService;

    public UserCommandListener(PassengerService passengerService, DriverService driverService) {
        this.passengerService = passengerService;
        this.driverService = driverService;
    }

    @RabbitListener(queues = MessagingTopology.PASSENGER_EXISTS_QUEUE)
    public PassengerExistsReply passengerExists(PassengerExistsCommand command) {
        return new PassengerExistsReply(passengerService.exists(command.passengerId()));
    }

    @RabbitListener(queues = MessagingTopology.DRIVER_RESERVE_QUEUE)
    public DriverReservationReply reserveDriver(ReserveDriverCommand command) {
        return driverService.reserveAvailableDriver();
    }

    @RabbitListener(queues = MessagingTopology.DRIVER_STATUS_QUEUE)
    public DriverStatusReply updateDriverStatus(DriverStatusCommand command) {
        boolean updated = driverService.updateStatusFromCommand(command.driverId(), command.status());
        return new DriverStatusReply(command.driverId(), command.status(), updated);
    }
}
