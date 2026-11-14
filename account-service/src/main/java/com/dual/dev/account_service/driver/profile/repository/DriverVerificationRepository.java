
package com.dual.dev.account_service.driver.profile.repository;

import com.dual.dev.account_service.driver.profile.entity.DriverVerification;

import com.dual.dev.account_service.driver.profile.enums.DriverVerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverVerificationRepository
        extends JpaRepository<DriverVerification, UUID> {

    // Find the latest verification attempt for a driver.
    Optional<DriverVerification>
    findFirstByDriver_IdOrderByCreatedAtDescIdDesc(UUID driverId);

    // Retrieve all verification attempts, newest first.
    List<DriverVerification>
    findAllByDriver_IdOrderByCreatedAtDesc(UUID driverId);

    // Retrieve applications with a specific status, using pagination.
    Page<DriverVerification>
    findAllByStatus(DriverVerificationStatus status, Pageable pageable);

    // Check whether a driver has a verification with a particular status.
    boolean existsByDriver_IdAndStatus(
            UUID driverId,
            DriverVerificationStatus status
    );
}
