
package com.dual.dev.shipment_service.messaging.outbox.service;

import com.dual.dev.shipment_service.messaging.config.RabbitMQConfig;
import com.dual.dev.shipment_service.messaging.event.EventEnvelope;
import com.dual.dev.shipment_service.messaging.outbox.entity.OutboxEvent;
import com.dual.dev.shipment_service.messaging.outbox.repository.OutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxRepository outboxRepository;

    private final JsonMapper jsonMapper;


    @Transactional(propagation = Propagation.MANDATORY)
    public void saveEvent(
            EventEnvelope<?> envelope,
            String routingKey
    ) {

        try {

            // 1. Convert the event envelope into JSON
            String eventJson =
                    jsonMapper.writeValueAsString(envelope);


            // 2. Create an Outbox entity
            OutboxEvent outboxEvent = OutboxEvent.pending(
                    envelope,
                    RabbitMQConfig.EVENTS_EXCHANGE,
                    routingKey,
                    eventJson
            );


            // 3. Save the event into PostgreSQL
            outboxRepository.save(outboxEvent);

            log.debug(
                    "Event saved to Outbox. Event type: {}, Event ID: {}",
                    envelope.eventType(),
                    envelope.eventId()
            );

        } catch (JacksonException exception) {

            throw new IllegalStateException(
                    "Failed to serialize event: "
                            + envelope.eventType(),
                    exception
            );
        }
    }
}
