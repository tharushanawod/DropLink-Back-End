package com.dual.dev.account_service.driver.profile.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DriverResponse(

        UUID id,

        String licenseNumber,

        String licenseClass,

        String licenseIssuingCountry,

        LocalDate licenseExpiryDate,

        DriverVerificationResponse latestVerification,

        Instant createdAt,

        Instant updatedAt

) {}