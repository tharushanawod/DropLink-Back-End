package com.dual.dev.account_service.branch_staff.dto.request;

import com.dual.dev.account_service.branch_staff.enums.BranchStaffDesignation;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BranchStaffUpdateRequest(

        @NotNull(message = "Branch ID is required.")
        UUID branchId,

        @NotNull(message = "Staff designation is required.")
        BranchStaffDesignation designation

) {
}
