package com.dual.dev.shipment_service.messaging.outbox.repository;

import com.dual.dev.shipment_service.messaging.outbox.entity.OutboxEvent;
import com.dual.dev.shipment_service.messaging.outbox.enums.OutboxStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository
        extends JpaRepository<OutboxEvent, UUID> {


    // Retrieve events eligible for publishing
    List<OutboxEvent>
    findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            OutboxStatus status,
            Instant currentTime
    );


    // Count pending events
    long countByStatus(OutboxStatus status);

}