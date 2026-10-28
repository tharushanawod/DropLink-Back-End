package com.dual.dev.shipment_service.shipment.service;

import com.dual.dev.shipment_service.shipment.dto.*;
import com.dual.dev.shipment_service.shipment.entity.Shipment;
import com.dual.dev.shipment_service.shipment.entity.Package;
import com.dual.dev.shipment_service.shipment.enums.ShipmentStatus;
import com.dual.dev.shipment_service.shipment.repository.ShipmentRepository;
import com.dual.dev.shipment_service.shipment.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final PackageRepository packageRepository;

    // 1. Create shipment
    @Transactional
    public ShipmentResponse createShipment(CreateShipmentRequest request) {

        Shipment shipment = Shipment.builder()
                .trackingNumber(generateTrackingNumber("DL-SHP-"))
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
                .deliveryBranchId(request.deliveryBranchId())
                .currentStatus(ShipmentStatus.CREATED)
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);

        List<Package> packages = request.packages().stream()
                .map(pkg -> Package.builder()
                        .shipment(savedShipment)
                        .weightKg(pkg.weightKg())
                        .lengthCm(pkg.lengthCm())
                        .widthCm(pkg.widthCm())
                        .heightCm(pkg.heightCm())
                        .packageType(pkg.packageType())
                        .declaredValue(pkg.declaredValue())
                        .description(pkg.description())
                        .trackingNumber(generateTrackingNumber("DL-PKG-"))
                        .currentBranchId(pkg.currentBranchId())
                        .currentStatus(pkg.currentStatus())
                        .deliveredAt(pkg.deliveredAt())
                        .build())
                .toList();

        // 4. Save all Packages
        List<Package> savedPackages = packageRepository.saveAll(packages);

        return toResponse(savedShipment , savedPackages);
    }

    // 2. Get shipment by ID
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(UUID id) {

        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Shipment not found"));

        List<Package> packages = packageRepository.findByShipmentId(id);

        return toResponse(shipment , packages);
    }

    // 3. Get shipment by tracking number
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByTrackingNumber(String trackingNumber) {

        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException("Shipment not found"));

        // 2. Find packages belonging to the shipment
        List<Package> packages =
                packageRepository.findByShipmentId(shipment.getId());

        // 3. Return shipment with packages
        return toResponse(shipment, packages);
    }

    // 4. Update shipment
    @Transactional
    public ShipmentResponse updateShipment(
            UUID id,
            UpdateShipmentRequest request
    ) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Shipment not found"));
        // 2. Find packages belonging to the shipment
        List<Package> packages =
                packageRepository.findByShipmentId(shipment.getId());

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

        // Update packages if provided
        if (request.packages() != null) {

            for (UpdatePackageRequest pkgRequest : request.packages()) {

                // Find the package within this shipment
                Package pkg = packages.stream()
                        .filter(p -> p.getId().equals(pkgRequest.id()))
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Package not found in this shipment"
                                ));

                // Update only provided fields
                if (pkgRequest.weightKg() != null)
                    pkg.setWeightKg(pkgRequest.weightKg());

                if (pkgRequest.lengthCm() != null)
                    pkg.setLengthCm(pkgRequest.lengthCm());

                if (pkgRequest.widthCm() != null)
                    pkg.setWidthCm(pkgRequest.widthCm());

                if (pkgRequest.heightCm() != null)
                    pkg.setHeightCm(pkgRequest.heightCm());

                if (pkgRequest.packageType() != null)
                    pkg.setPackageType(pkgRequest.packageType());

                if (pkgRequest.declaredValue() != null)
                    pkg.setDeclaredValue(pkgRequest.declaredValue());

                if (pkgRequest.description() != null)
                    pkg.setDescription(pkgRequest.description());

                if (pkgRequest.currentBranchId() != null)
                    pkg.setCurrentBranchId(pkgRequest.currentBranchId());

                if (pkgRequest.currentStatus() != null)
                    pkg.setCurrentStatus(pkgRequest.currentStatus());

                if (pkgRequest.deliveredAt() != null)
                    pkg.setDeliveredAt(pkgRequest.deliveredAt());
            }
        }

// Save changes
        shipmentRepository.save(shipment);
        packageRepository.saveAll(packages);

        return toResponse(shipment, packages);
    }

    // 5. Generate tracking number
    private String generateTrackingNumber(String prefix) {
        return prefix + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }

    // 6. Convert entity into response DTO
    private ShipmentResponse toResponse(Shipment shipment , List<Package> packages) {

        List<PackageResponse> packageResponses = packages.stream()
                .map(pkg -> new PackageResponse(
                        pkg.getId(),
                        pkg.getWeightKg(),
                        pkg.getLengthCm(),
                        pkg.getWidthCm(),
                        pkg.getHeightCm(),
                        pkg.getPackageType().name(),
                        pkg.getDeclaredValue(),
                        pkg.getDescription(),
                        pkg.getTrackingNumber(),
                        pkg.getCurrentBranchId(),
                        pkg.getCurrentStatus() != null ? pkg.getCurrentStatus().name() : null,
                        pkg.getDeliveredAt()
                ))
                .toList();

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
                shipment.getUpdatedAt(),
                packageResponses
        );
    }
}
