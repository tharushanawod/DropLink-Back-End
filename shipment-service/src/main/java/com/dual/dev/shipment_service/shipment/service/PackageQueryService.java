package com.dual.dev.shipment_service.shipment.service;

import com.dual.dev.shipment_service.shipment.dto.EligiblePackageResponse;
import com.dual.dev.shipment_service.shipment.entity.Package;
import com.dual.dev.shipment_service.shipment.enums.DispatchType;
import com.dual.dev.shipment_service.shipment.enums.PackageStatus;
import com.dual.dev.shipment_service.shipment.repository.PackageRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PackageQueryService {

    private final PackageRepository packageRepository;

    @Transactional(readOnly = true)
    public List<EligiblePackageResponse> getEligiblePackages(
            UUID branchId,
            DispatchType type) {

        List<Package> packages;

        if (type == DispatchType.LAST_MILE_DELIVERY) {

            packages = packageRepository
                    .findByCurrentBranchIdAndCurrentStatusAndShipment_DeliveryBranchId(
                            branchId,
                            PackageStatus.AT_BRANCH,
                            branchId
                    );

        } else if (type == DispatchType.BRANCH_TRANSFER) {

            packages = packageRepository
                    .findByCurrentBranchIdAndCurrentStatusAndShipment_DeliveryBranchIdNot(
                            branchId,
                            PackageStatus.AT_BRANCH,
                            branchId
                    );

        } else {
            throw new IllegalArgumentException(
                    "Unsupported dispatch type: " + type
            );
        }

        return packages.stream()
                .map(this::toResponse)
                .toList();
    }

    private EligiblePackageResponse toResponse(Package pkg) {

        var shipment = pkg.getShipment();

        return new EligiblePackageResponse(
                pkg.getId(),
                pkg.getTrackingNumber(),
                shipment.getTrackingNumber(),
                shipment.getReceiverName(),
                shipment.getReceiverPhone(),
                shipment.getDeliveryAddress(),
                pkg.getWeightKg(),
                pkg.getDeclaredValue(),
                pkg.getPackageType().name(),
                pkg.getCurrentBranchId(),
                shipment.getDeliveryBranchId(),
                pkg.getCurrentStatus().name()
        );
    }
}