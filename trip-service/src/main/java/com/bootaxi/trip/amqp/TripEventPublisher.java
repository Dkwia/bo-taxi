package com.bootaxi.trip.amqp;

import com.bootaxi.contracts.amqp.MessagingTopology;
import com.bootaxi.contracts.amqp.TripStatusChangedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TripEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public TripEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(TripStatusChangedEvent event) {
        rabbitTemplate.convertAndSend(
                MessagingTopology.TRIP_EVENTS_EXCHANGE,
                MessagingTopology.TRIP_STATUS_ROUTING_KEY,
                event
        );
    }
}
