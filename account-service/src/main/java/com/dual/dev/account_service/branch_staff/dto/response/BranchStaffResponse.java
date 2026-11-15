
package com.dual.dev.account_service.branch_staff.dto.response;

import com.dual.dev.account_service.branch_staff.enums.BranchStaffDesignation;

import java.time.Instant;
import java.util.UUID;

public record BranchStaffResponse(

        UUID id,

        UUID accountId,

        String email,

        String firstName,

        String lastName,

        String phoneNumber,

        UUID branchId,

        String branchCode,

        String branchName,

        BranchStaffDesignation designation,

        boolean active,

        Instant assignedAt,

        Instant deactivatedAt,

        Instant createdAt,

        Instant updatedAt

) {
}
