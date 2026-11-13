package com.dual.dev.account_service.driver.profile.dto.response;

import com.dual.dev.account_service.driver.profile.enums.DriverVerificationStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DriverResponse(

        UUID id,

        UUID accountId,

        String email,

        String firstName,

        String lastName,

        String phoneNumber,

        String licenseNumber,

        String licenseClass,

        String licenseIssuingCountry,

        LocalDate licenseExpiryDate,

        DriverVerificationStatus verificationStatus,

        String rejectionReason,

        Instant submittedAt,

        Instant reviewedAt,

        UUID reviewedByAccountId,

        Instant createdAt,

        Instant updatedAt

) {}