package com.dual.dev.account_service.driver.profile.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DriverProfileRequest(

        @NotBlank(message = "Licence number is required")
        @Size(max = 50)
        String licenseNumber,

        @NotBlank(message = "Licence class is required")
        @Size(max = 50)
        String licenseClass,

        @NotBlank(message = "Licence issuing country is required")
        @Size(max = 100)
        String licenseIssuingCountry,

        @NotNull(message = "Licence expiry date is required")
        @FutureOrPresent(message = "Licence expiry date cannot be in the past")
        LocalDate licenseExpiryDate

) {}