package com.dual.dev.shipment_service.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Shared DropLink event exchange
    public static final String EVENTS_EXCHANGE =
            "droplink.events";

    // Temporary queue for our connection test
    public static final String TEST_QUEUE =
            "shipment.test.queue";

    // Temporary routing key
    public static final String TEST_ROUTING_KEY =
            "shipment.test";

    @Bean
    public TopicExchange droplinkEventsExchange() {
        return new TopicExchange(
                EVENTS_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Queue shipmentTestQueue() {
        return QueueBuilder
                .durable(TEST_QUEUE)
                .build();
    }

    @Bean
    public Binding shipmentTestBinding(
            Queue shipmentTestQueue,
            TopicExchange droplinkEventsExchange
    ) {
        return BindingBuilder
                .bind(shipmentTestQueue)
                .to(droplinkEventsExchange)
                .with(TEST_ROUTING_KEY);
    }
}
