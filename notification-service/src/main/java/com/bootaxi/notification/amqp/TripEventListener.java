package com.bootaxi.notification.amqp;

import com.bootaxi.contracts.amqp.MessagingTopology;
import com.bootaxi.contracts.amqp.TripStatusChangedEvent;
import com.bootaxi.notification.service.NotificationTaskService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TripEventListener {

    private final NotificationTaskService notificationTaskService;

    public TripEventListener(NotificationTaskService notificationTaskService) {
        this.notificationTaskService = notificationTaskService;
    }

    @RabbitListener(queues = MessagingTopology.TRIP_STATUS_QUEUE)
    public void onTripStatusChanged(TripStatusChangedEvent event) {
        notificationTaskService.createFromTripEvent(event);
    }
}
