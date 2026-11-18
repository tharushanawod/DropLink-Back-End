
package com.dual.dev.shipment_service.messaging.outbox.entity;

import com.dual.dev.shipment_service.messaging.event.EventEnvelope;
import com.dual.dev.shipment_service.messaging.outbox.enums.OutboxStatus;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(
                        name = "idx_outbox_status_next_attempt",
                        columnList = "status, next_attempt_at"
                ),
                @Index(
                        name = "idx_outbox_aggregate",
                        columnList = "aggregate_type, aggregate_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @Column(
            name = "event_id",
            nullable = false,
            updatable = false
    )
    private UUID eventId;

    @Column(
            name = "event_type",
            nullable = false,
            length = 100
    )
    private String eventType;

    @Column(
            name = "event_version",
            nullable = false
    )
    private int eventVersion;

    @Column(
            name = "exchange_name",
            nullable = false,
            length = 150
    )
    private String exchangeName;

    @Column(
            name = "routing_key",
            nullable = false,
            length = 150
    )
    private String routingKey;

    @Column(
            name = "aggregate_type",
            nullable = false,
            length = 50
    )
    private String aggregateType;

    @Column(
            name = "aggregate_id",
            nullable = false
    )
    private UUID aggregateId;

    @Column(
            name = "source_service",
            nullable = false,
            length = 100
    )
    private String source;

    @Column(
            name = "occurred_at",
            nullable = false
    )
    private Instant occurredAt;

    // Complete serialized EventEnvelope JSON
    @Column(
            name = "event_json",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String eventJson;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private OutboxStatus status;

    @Column(
            name = "attempt_count",
            nullable = false
    )
    private int attemptCount;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(
            name = "last_error",
            columnDefinition = "TEXT"
    )
    private String lastError;


    // Factory method for creating a pending event
    public static OutboxEvent pending(
            EventEnvelope<?> envelope,
            String exchangeName,
            String routingKey,
            String eventJson
    ) {

        OutboxEvent event = new OutboxEvent();

        event.eventId = envelope.eventId();
        event.eventType = envelope.eventType();
        event.eventVersion = envelope.eventVersion();

        event.exchangeName = exchangeName;
        event.routingKey = routingKey;

        event.aggregateType = envelope.aggregateType();
        event.aggregateId = envelope.aggregateId();

        event.source = envelope.source();
        event.occurredAt = envelope.occurredAt();

        event.eventJson = eventJson;

        event.status = OutboxStatus.PENDING;
        event.attemptCount = 0;

        Instant now = Instant.now();

        event.createdAt = now;
        event.nextAttemptAt = now;

        return event;
    }


    // Called after RabbitMQ confirms publication
    public void markPublished(Instant publishedAt) {

        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        this.nextAttemptAt = null;
        this.lastError = null;
    }


    // Called when publishing fails
    public void recordPublishFailure(
            String errorMessage,
            Instant retryAt,
            boolean permanentlyFailed
    ) {

        this.attemptCount++;

        this.lastError = truncateError(errorMessage);

        if (permanentlyFailed) {

            this.status = OutboxStatus.FAILED;
            this.nextAttemptAt = null;

        } else {

            this.status = OutboxStatus.PENDING;
            this.nextAttemptAt = retryAt;
        }
    }


    private String truncateError(String error) {

        if (error == null) {
            return "Unknown publishing error";
        }

        return error.length() > 4000
                ? error.substring(0, 4000)
                : error;
    }
}
