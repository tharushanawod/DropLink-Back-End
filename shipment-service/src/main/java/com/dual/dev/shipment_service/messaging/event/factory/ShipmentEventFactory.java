package com.dual.dev.shipment_service.messaging.event.factory;

import com.dual.dev.shipment_service.messaging.event.EventEnvelope;
import com.dual.dev.shipment_service.messaging.event.ShipmentEventTypes;
import com.dual.dev.shipment_service.messaging.event.payload.PackageRegisteredPayload;
import com.dual.dev.shipment_service.messaging.event.payload.ShipmentCreatedPayload;
import com.dual.dev.shipment_service.shipment.entity.Shipment;
import com.dual.dev.shipment_service.shipment.entity.Package;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ShipmentEventFactory {

    // Create shipment-created event
    public EventEnvelope<ShipmentCreatedPayload> createShipmentCreatedEvent(
            Shipment shipment,
            int packageCount
    ) {

        ShipmentCreatedPayload payload =
                new ShipmentCreatedPayload(
                        shipment.getId(),
                        shipment.getTrackingNumber(),
                        shipment.getCurrentStatus().name(),
                        shipment.getDeliveryBranchId(),
                        packageCount
                );

        return new EventEnvelope<>(
                UUID.randomUUID(),
                ShipmentEventTypes.SHIPMENT_CREATED,
                ShipmentEventTypes.EVENT_VERSION,
                Instant.now(),
                ShipmentEventTypes.SOURCE,
                ShipmentEventTypes.SHIPMENT_AGGREGATE,
                shipment.getId(),
                payload
        );
    }


    // Create package-registered event
    public EventEnvelope<PackageRegisteredPayload> createPackageRegisteredEvent(
            Shipment shipment,
            Package pkg
    ) {

        PackageRegisteredPayload payload =
                new PackageRegisteredPayload(
                        pkg.getId(),
                        shipment.getId(),
                        pkg.getTrackingNumber(),
                        pkg.getCurrentStatus() != null
                                ? pkg.getCurrentStatus().name()
                                : null,
                        pkg.getCurrentBranchId(),
                        shipment.getDeliveryBranchId()
                );

        return new EventEnvelope<>(
                UUID.randomUUID(),
                ShipmentEventTypes.PACKAGE_REGISTERED,
                ShipmentEventTypes.EVENT_VERSION,
                Instant.now(),
                ShipmentEventTypes.SOURCE,
                ShipmentEventTypes.PACKAGE_AGGREGATE,
                pkg.getId(),
                payload
        );
    }
}
