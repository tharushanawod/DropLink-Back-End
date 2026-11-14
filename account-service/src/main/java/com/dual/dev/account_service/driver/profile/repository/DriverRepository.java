package com.dual.dev.account_service.driver.profile.repository;

import com.dual.dev.account_service.driver.profile.entity.Driver;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    Optional<Driver> findByAccount_Id(UUID accountId);

    boolean existsByAccount_Id(UUID accountId);

    boolean existsByLicenseNumberAndLicenseIssuingCountry(
            String licenseNumber,
            String licenseIssuingCountry
    );

    boolean existsByLicenseNumberAndLicenseIssuingCountryAndIdNot(
            String licenseNumber,
            String licenseIssuingCountry,
            UUID driverId
    );
}