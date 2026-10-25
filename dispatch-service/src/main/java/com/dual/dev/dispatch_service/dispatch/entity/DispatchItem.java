package com.dual.dev.dispatch_service.dispatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "dispatch_items",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"dispatch_id", "package_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Many dispatch items belong to one dispatch
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispatch_id", nullable = false)
    private Dispatch dispatch;

    // Package belongs to Shipment Service
    @Column(name = "package_id", nullable = false)
    private UUID packageId;

    // Stops belong to Dispatch Service
    private UUID pickupStopId;
    private UUID dropoffStopId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DispatchItemStatus status;

    private LocalDateTime loadedAt;
    private LocalDateTime unloadedAt;
}