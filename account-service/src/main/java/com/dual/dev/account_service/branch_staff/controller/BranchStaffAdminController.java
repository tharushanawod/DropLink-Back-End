package com.dual.dev.account_service.branch_staff.controller;

import com.dual.dev.account_service.branch_staff.dto.request.BranchStaffAssignRequest;
import com.dual.dev.account_service.branch_staff.dto.request.BranchStaffUpdateRequest;
import com.dual.dev.account_service.branch_staff.dto.response.BranchStaffResponse;
import com.dual.dev.account_service.branch_staff.service.BranchStaffService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') and @accountStatusChecker.isActive(authentication)")
public class BranchStaffAdminController {

    private final BranchStaffService branchStaffService;


    // Assign an existing account to a branch
    @PostMapping("/branches/{branchId}/staff")
    public ResponseEntity<BranchStaffResponse> assignStaff(
            @PathVariable UUID branchId,
            @Valid @RequestBody BranchStaffAssignRequest request) {

        BranchStaffResponse response =
                branchStaffService.assignStaff(branchId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // List staff members belonging to a branch
    @GetMapping("/branches/{branchId}/staff")
    public Page<BranchStaffResponse> getBranchStaff(
            @PathVariable UUID branchId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return branchStaffService.getBranchStaff(
                branchId,
                active,
                pageable
        );
    }


    // Retrieve one staff assignment
    @GetMapping("/branch-staff/{staffId}")
    public BranchStaffResponse getStaff(
            @PathVariable UUID staffId) {

        return branchStaffService.getStaff(staffId);
    }


    // Transfer staff or update their designation
    @PutMapping("/branch-staff/{staffId}")
    public BranchStaffResponse updateStaff(
            @PathVariable UUID staffId,
            @Valid @RequestBody BranchStaffUpdateRequest request) {

        return branchStaffService.updateStaff(
                staffId,
                request
        );
    }


    // Deactivate staff
    @PatchMapping("/branch-staff/{staffId}/deactivate")
    public BranchStaffResponse deactivateStaff(
            @PathVariable UUID staffId) {

        return branchStaffService.deactivateStaff(staffId);
    }


    // Reactivate staff
    @PatchMapping("/branch-staff/{staffId}/activate")
    public BranchStaffResponse activateStaff(
            @PathVariable UUID staffId) {

        return branchStaffService.activateStaff(staffId);
    }
}
