package com.dual.dev.shipment_service.shipment.service;

import com.dual.dev.shipment_service.shipment.dto.CreateShipmentRequest;
import com.dual.dev.shipment_service.shipment.dto.ShipmentResponse;
import com.dual.dev.shipment_service.shipment.dto.UpdateShipmentRequest;
import com.dual.dev.shipment_service.shipment.entity.Shipment;
import com.dual.dev.shipment_service.shipment.enums.ShipmentStatus;
import com.dual.dev.shipment_service.shipment.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    public ShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    // 1. Create shipment
    @Transactional
    public ShipmentResponse createShipment(CreateShipmentRequest request) {

        Shipment shipment = Shipment.builder()
                .trackingNumber(generateTrackingNumber())
                .senderName(request.senderName())
                .senderPhone(request.senderPhone())
                .receiverName(request.receiverName())
                .receiverPhone(request.receiverPhone())
                .pickupAddress(request.pickupAddress())
                .deliveryAddress(request.deliveryAddress())
                .pickupLatitude(request.pickupLatitude())
                .pickupLongitude(request.pickupLongitude())
                .deliveryLatitude(request.deliveryLatitude())
                .deliveryLongitude(request.deliveryLongitude())
                .currentStatus(ShipmentStatus.CREATED)
                .build();

        Shipment savedShipment = shipmentRepository.saveAndFlush(shipment);

        return toResponse(savedShipment);
    }

    // 2. Get shipment by ID
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(Long id) {

        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Shipment not found"));

        return toResponse(shipment);
    }

    // 3. Get shipment by tracking number
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByTrackingNumber(String trackingNumber) {

        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException("Shipment not found"));

        return toResponse(shipment);
    }

    // 4. Update shipment
    @Transactional
    public ShipmentResponse updateShipment(
            Long id,
            UpdateShipmentRequest request
    ) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Shipment not found"));

        if (shipment.getCurrentStatus() != ShipmentStatus.CREATED) {
            throw new IllegalStateException(
                    "Only CREATED shipments can be updated"
            );
        }

        if (request.senderName() != null)
            shipment.setSenderName(request.senderName());

        if (request.senderPhone() != null)
            shipment.setSenderPhone(request.senderPhone());

        if (request.receiverName() != null)
            shipment.setReceiverName(request.receiverName());

        if (request.receiverPhone() != null)
            shipment.setReceiverPhone(request.receiverPhone());

        if (request.pickupAddress() != null)
            shipment.setPickupAddress(request.pickupAddress());

        if (request.deliveryAddress() != null)
            shipment.setDeliveryAddress(request.deliveryAddress());

        if (request.pickupLatitude() != null)
            shipment.setPickupLatitude(request.pickupLatitude());

        if (request.pickupLongitude() != null)
            shipment.setPickupLongitude(request.pickupLongitude());

        if (request.deliveryLatitude() != null)
            shipment.setDeliveryLatitude(request.deliveryLatitude());

        if (request.deliveryLongitude() != null)
            shipment.setDeliveryLongitude(request.deliveryLongitude());

        return toResponse(shipmentRepository.saveAndFlush(shipment));
    }

    // 5. Generate tracking number
    private String generateTrackingNumber() {
        return "DL-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }

    // 6. Convert entity into response DTO
    private ShipmentResponse toResponse(Shipment shipment) {

        return new ShipmentResponse(
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getSenderName(),
                shipment.getSenderPhone(),
                shipment.getReceiverName(),
                shipment.getReceiverPhone(),
                shipment.getPickupAddress(),
                shipment.getDeliveryAddress(),
                shipment.getPickupLatitude(),
                shipment.getPickupLongitude(),
                shipment.getDeliveryLatitude(),
                shipment.getDeliveryLongitude(),
                shipment.getCurrentStatus().name(),
                shipment.getEstimatedDelivery(),
                shipment.getActualDelivery(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt()
        );
    }
}
