package com.dual.dev.dispatch_service.dispatch.entity;

import com.dual.dev.dispatch_service.dispatch.enums.DispatchCreationMode;
import com.dual.dev.dispatch_service.dispatch.enums.DispatchStatus;
import com.dual.dev.dispatch_service.dispatch.enums.DispatchType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dispatches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String dispatchNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DispatchType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DispatchStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DispatchCreationMode creationMode;

    // References to other services
    private UUID originBranchId;
    private UUID driverId;
    private UUID vehicleId;

    // Route managed inside Dispatch Service
    private UUID routeId;

    private LocalDateTime scheduledDeparture;
    private LocalDateTime actualDeparture;
    private LocalDateTime completedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}