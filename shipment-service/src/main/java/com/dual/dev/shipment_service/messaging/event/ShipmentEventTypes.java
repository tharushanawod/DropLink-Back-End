
package com.dual.dev.shipment_service.messaging.event;

public final class ShipmentEventTypes {

    private ShipmentEventTypes() {
    }

    // Event types
    public static final String SHIPMENT_CREATED =
            "SHIPMENT_CREATED";

    public static final String PACKAGE_REGISTERED =
            "PACKAGE_REGISTERED";


    // RabbitMQ routing keys
    public static final String SHIPMENT_CREATED_ROUTING_KEY =
            "shipment.created";

    public static final String PACKAGE_REGISTERED_ROUTING_KEY =
            "shipment.package.registered";


    // Event metadata
    public static final String SOURCE =
            "shipment-service";

    public static final String SHIPMENT_AGGREGATE =
            "SHIPMENT";

    public static final String PACKAGE_AGGREGATE =
            "PACKAGE";

    public static final int EVENT_VERSION = 1;
}
