package com.dual.dev.shipment_service.messaging.event.payload;

import java.util.UUID;

public record ShipmentCreatedPayload(

        UUID shipmentId,

        String shipmentTrackingNumber,

        String shipmentStatus,

        UUID deliveryBranchId,

        int packageCount

) {
}
