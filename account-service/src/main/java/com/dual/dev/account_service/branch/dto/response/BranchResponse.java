package com.dual.dev.account_service.branch.dto.response;

import java.time.Instant;
import java.util.UUID;

public record BranchResponse(

        UUID id,

        String branchCode,

        String name,

        String addressLine1,

        String addressLine2,

        String city,

        String district,

        String postalCode,

        String country,

        String contactPhone,

        String contactEmail,

        boolean active,

        Instant createdAt,

        Instant updatedAt

) {
}
