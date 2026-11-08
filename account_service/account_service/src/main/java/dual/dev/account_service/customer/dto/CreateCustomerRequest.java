package com.dual.dev.account_service.customer.dto;

import jakarta.validation.constraints.*;

public record CreateCustomerRequest(

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @Pattern(
                regexp = "^0[0-9]{9}$",
                message = "Enter a valid Sri Lankan phone number"
        )
        String phone
) {}