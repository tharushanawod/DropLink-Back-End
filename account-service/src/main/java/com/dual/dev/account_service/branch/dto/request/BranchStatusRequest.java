package com.dual.dev.account_service.branch.dto.request;

import jakarta.validation.constraints.NotNull;

public record BranchStatusRequest(

        @NotNull(message = "Active status is required.")
        Boolean active

) {
}
