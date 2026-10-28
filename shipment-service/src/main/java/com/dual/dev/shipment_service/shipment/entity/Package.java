package com.dual.dev.shipment_service.shipment.entity;

import com.dual.dev.shipment_service.shipment.enums.PackageStatus;
import com.dual.dev.shipment_service.shipment.enums.PackageType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weightKg;

    @Column(precision = 10, scale = 2)
    private BigDecimal lengthCm;

    @Column(precision = 10, scale = 2)
    private BigDecimal widthCm;

    @Column(precision = 10, scale = 2)
    private BigDecimal heightCm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PackageType packageType;

    @Column(precision = 12, scale = 2)
    private BigDecimal declaredValue;

    @Column(length = 500)
    private String description;

    // Unique tracking number for each package
    @Column(name = "tracking_number", nullable = false, unique = true, length = 50)
    private String trackingNumber;

    // Branch currently holding the package
    @Column(name = "current_branch_id")
    private UUID currentBranchId;

    // Current operational status
    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 30)
    private PackageStatus currentStatus;

    // When the package was delivered
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}