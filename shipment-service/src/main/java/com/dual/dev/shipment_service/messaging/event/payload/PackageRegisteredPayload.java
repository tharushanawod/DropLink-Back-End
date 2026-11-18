package com.dual.dev.shipment_service.messaging.event.payload;

import java.util.UUID;

public record PackageRegisteredPayload(

        UUID packageId,

        UUID shipmentId,

        String packageTrackingNumber,

        String initialStatus,

        UUID initialBranchId,

        UUID destinationBranchId

) {
}
