package com.dual.dev.account_service.customer.dto;

import jakarta.validation.constraints.*;

public record UpdateCustomerRequest(

        @Size(max = 100)
        @Pattern(
                regexp = ".*\\S.*",
                message = "First name cannot be blank"
        )
        String firstName,

        @Size(max = 100)
        @Pattern(
                regexp = ".*\\S.*",
                message = "Last name cannot be blank"
        )
        String lastName,

        @Pattern(
                regexp = "^0[0-9]{9}$",
                message = "Enter a valid Sri Lankan phone number"
        )
        String phone
) {}