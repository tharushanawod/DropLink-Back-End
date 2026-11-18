package com.dual.dev.shipment_service.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(

        UUID eventId,

        String eventType,

        int eventVersion,

        Instant occurredAt,

        String source,

        String aggregateType,

        UUID aggregateId,

        T payload

) {
}
