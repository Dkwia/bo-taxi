package com.bootaxi.trip.config;

import com.bootaxi.contracts.amqp.MessagingTopology;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

        return new Declarables(
                new Queue(MessagingTopology.PASSENGER_EXISTS_QUEUE, true),
                new Queue(MessagingTopology.DRIVER_RESERVE_QUEUE, true),
                new Queue(MessagingTopology.DRIVER_STATUS_QUEUE, true),
                notificationQueue,
                exchange,
                binding
        );
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                                  Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setReplyTimeout(5000);
        return rabbitTemplate;
    }
}
