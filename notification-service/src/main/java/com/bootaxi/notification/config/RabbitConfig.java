package com.bootaxi.notification.config;

import com.bootaxi.contracts.amqp.MessagingTopology;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    Declarables topology() {
        DirectExchange exchange = new DirectExchange(MessagingTopology.TRIP_EVENTS_EXCHANGE, true, false);
        Queue notificationQueue = new Queue(MessagingTopology.TRIP_STATUS_QUEUE, true);
        Binding binding = BindingBuilder.bind(notificationQueue)
                .to(exchange)
                .with(MessagingTopology.TRIP_STATUS_ROUTING_KEY);

        return new Declarables(notificationQueue, exchange, binding);
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
