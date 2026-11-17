
package com.dual.dev.shipment_service.messaging.publisher;

import com.dual.dev.shipment_service.messaging.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMqTestPublisher implements ApplicationRunner {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void run(ApplicationArguments args) {

        String message = "Hello from Shipment Service!";

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EVENTS_EXCHANGE,
                RabbitMQConfig.TEST_ROUTING_KEY,
                message
        );

        System.out.println(
                "Test message published to RabbitMQ: " + message
        );
    }
}
