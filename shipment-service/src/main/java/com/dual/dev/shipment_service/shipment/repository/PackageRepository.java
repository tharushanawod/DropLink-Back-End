package com.dual.dev.shipment_service.shipment.repository;

import com.dual.dev.shipment_service.shipment.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface PackageRepository extends JpaRepository<Package, UUID> {
}
