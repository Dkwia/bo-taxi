package com.bootaxi.user.config;

import com.bootaxi.contracts.amqp.MessagingTopology;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    Declarables userQueues() {
        return new Declarables(
                new Queue(MessagingTopology.PASSENGER_EXISTS_QUEUE, true),
                new Queue(MessagingTopology.DRIVER_RESERVE_QUEUE, true),
                new Queue(MessagingTopology.DRIVER_STATUS_QUEUE, true),
                new Queue(MessagingTopology.TRIP_STATUS_QUEUE, true),
                new DirectExchange(MessagingTopology.TRIP_EVENTS_EXCHANGE, true, false)
        );
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
