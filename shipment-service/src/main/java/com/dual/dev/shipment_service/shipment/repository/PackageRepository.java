package com.dual.dev.shipment_service.shipment.repository;

import com.dual.dev.shipment_service.shipment.entity.Package;
import com.dual.dev.shipment_service.shipment.enums.PackageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface PackageRepository extends JpaRepository<Package, UUID> {

    List<Package> findByShipmentId (UUID shipmentId);

    // Packages ready for last-mile delivery
    List<Package> findByCurrentBranchIdAndCurrentStatusAndShipment_DeliveryBranchId(
            UUID currentBranchId,
            PackageStatus status,
            UUID deliveryBranchId
    );

    // Packages that may need a branch transfer
    List<Package> findByCurrentBranchIdAndCurrentStatusAndShipment_DeliveryBranchIdNot(
            UUID currentBranchId,
            PackageStatus status,
            UUID deliveryBranchId
    );
}
