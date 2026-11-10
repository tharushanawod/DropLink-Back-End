package com.dual.dev.account_service.customer.address.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateSavedAddressRequest(

        @NotBlank(message = "Address label is required")
        @Size(max = 50)
        String label,

        @NotBlank(message = "Recipient name is required")
        @Size(max = 100)
        String recipientName,

        @NotBlank(message = "Recipient phone is required")
        @Pattern(
                regexp = "^0[0-9]{9}$",
                message = "Phone number must contain 10 digits and start with 0"
        )
        String recipientPhone,

        @NotBlank(message = "Address line 1 is required")
        @Size(max = 255)
        String addressLine1,

        @Size(max = 255)
        String addressLine2,

        @NotBlank(message = "City is required")
        @Size(max = 100)
        String city,

        @Size(max = 100)
        String district,

        @Size(max = 20)
        String postalCode,

        @NotBlank(message = "Country is required")
        @Size(max = 100)
        String country,

        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        BigDecimal longitude

) {}