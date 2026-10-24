package com.dual.dev.shipment_service.shipment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ShipmentResponse(

        java.util.UUID id,
        String trackingNumber,

        String senderName,
        String senderPhone,

        String receiverName,
        String receiverPhone,

        String pickupAddress,
        String deliveryAddress,

        BigDecimal pickupLatitude,
        BigDecimal pickupLongitude,

        BigDecimal deliveryLatitude,
        BigDecimal deliveryLongitude,

        String currentStatus,

        LocalDateTime estimatedDelivery,
        LocalDateTime actualDelivery,

        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        List<PackageResponse> packages
) {
}