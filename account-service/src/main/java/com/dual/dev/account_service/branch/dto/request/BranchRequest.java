package com.dual.dev.account_service.branch.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BranchRequest(

        @NotBlank(message = "Branch code is required.")
        @Size(max = 30)
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "Branch code can contain only letters, numbers, hyphens and underscores."
        )
        String branchCode,

        @NotBlank(message = "Branch name is required.")
        @Size(max = 150)
        String name,

        @NotBlank(message = "Address is required.")
        @Size(max = 200)
        String addressLine1,

        @Size(max = 200)
        String addressLine2,

        @NotBlank(message = "City is required.")
        @Size(max = 100)
        String city,

        @Size(max = 100)
        String district,

        @Size(max = 20)
        String postalCode,

        @NotBlank(message = "Country is required.")
        @Size(max = 100)
        String country,

        @Pattern(
                regexp = "^\\+?[0-9()\\- ]{7,25}$",
                message = "Invalid contact phone number."
        )
        String contactPhone,

        @Email(message = "Invalid contact email address.")
        @Size(max = 150)
        String contactEmail

) {
}
