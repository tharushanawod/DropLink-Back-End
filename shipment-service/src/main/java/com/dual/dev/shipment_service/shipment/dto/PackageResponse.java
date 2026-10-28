package com.dual.dev.shipment_service.shipment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PackageResponse(
        UUID id,
        BigDecimal weightKg,
        BigDecimal lengthCm,
        BigDecimal widthCm,
        BigDecimal heightCm,
        String packageType,
        BigDecimal declaredValue,
        String description,
        String trackingNumber,
        UUID currentBranchId,
        String currentStatus,
        LocalDateTime deliveredAt
) {}
