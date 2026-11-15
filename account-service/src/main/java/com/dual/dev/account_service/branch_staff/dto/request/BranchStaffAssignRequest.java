package com.dual.dev.account_service.branch_staff.dto.request;

import com.dual.dev.account_service.branch_staff.enums.BranchStaffDesignation;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BranchStaffAssignRequest(

        @NotNull(message = "Account ID is required.")
        UUID accountId,

        @NotNull(message = "Staff designation is required.")
        BranchStaffDesignation designation

) {
}
