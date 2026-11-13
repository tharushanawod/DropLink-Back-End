package com.dual.dev.account_service.driver.profile.repository;

import com.dual.dev.account_service.driver.profile.entity.Driver;
import com.dual.dev.account_service.driver.profile.enums.DriverVerificationStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    // Find a driver profile belonging to an Account
    Optional<Driver> findByAccount_Id(UUID accountId);

    // Check whether an Account already has a driver profile
    boolean existsByAccount_Id(UUID accountId);

    // Check licence uniqueness
    boolean existsByLicenseNumberAndLicenseIssuingCountry(
            String licenseNumber,
            String licenseIssuingCountry
    );

    // Check licence uniqueness while excluding the current driver
    boolean existsByLicenseNumberAndLicenseIssuingCountryAndIdNot(
            String licenseNumber,
            String licenseIssuingCountry,
            UUID driverId
    );

    // Retrieve drivers filtered by verification status
    Page<Driver> findAllByVerificationStatus(
            DriverVerificationStatus verificationStatus,
            Pageable pageable
    );
}