
package com.dual.dev.shipment_service.messaging.listener;

import com.dual.dev.shipment_service.messaging.config.RabbitMQConfig;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitMqTestListener {

    @RabbitListener(
            queues = RabbitMQConfig.TEST_QUEUE
    )
    public void receiveMessage(String message) {

        log.info(
                "Message received from RabbitMQ: {}",
                message
        );
    }
}
