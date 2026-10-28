package com.dual.dev.dispatch_service.dispatch.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record EligiblePackageResponse(

        UUID packageId,
        String trackingNumber,

        // Shipment information
        String shipmentTrackingNumber,
        String receiverName,
        String receiverPhone,
        String deliveryAddress,

        // Package information
        BigDecimal weightKg,
        BigDecimal declaredValue,
        String packageType,

        // Delivery information
        UUID currentBranchId,
        UUID deliveryBranchId,
        String currentStatus

) {}