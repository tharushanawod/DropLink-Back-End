package com.dual.dev.shipment_service.shipment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record EligiblePackageResponse(
        UUID packageId,
        String trackingNumber,
        String shipmentTrackingNumber,
        String receiverName,
        String receiverPhone,
        String deliveryAddress,
        BigDecimal weightKg,
        BigDecimal declaredValue,
        String packageType,
        UUID currentBranchId,
        UUID deliveryBranchId,
        String currentStatus
) {}