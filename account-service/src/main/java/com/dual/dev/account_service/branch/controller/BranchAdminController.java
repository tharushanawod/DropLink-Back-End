
package com.dual.dev.account_service.branch.controller;

import com.dual.dev.account_service.account.dto.response.AccountResponse;
import com.dual.dev.account_service.branch.dto.request.BranchRequest;
import com.dual.dev.account_service.branch.dto.request.BranchStatusRequest;
import com.dual.dev.account_service.branch.dto.response.BranchResponse;
import com.dual.dev.account_service.branch.service.BranchService;

import com.dual.dev.account_service.common.dto.PageResponse;
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
@RequestMapping("/api/v1/admin/branches")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') and @accountStatusChecker.isActive(authentication)")
public class BranchAdminController {

    private final BranchService branchService;


    // Create branch
    @PostMapping
    public ResponseEntity<BranchResponse> createBranch(
            @Valid @RequestBody BranchRequest request) {

        BranchResponse response = branchService.createBranch(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // Retrieve branches
    @GetMapping
    public PageResponse<BranchResponse> getBranches(
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        Page<BranchResponse> branchPage = branchService.getBranches(active, pageable);

        return new PageResponse<>(
                branchPage.getContent(),
                branchPage.getNumber(),
                branchPage.getSize(),
                branchPage.getTotalElements(),
                branchPage.getTotalPages(),
                branchPage.isFirst(),
                branchPage.isLast()
        );
    }


    // Retrieve one branch
    @GetMapping("/{branchId}")
    public BranchResponse getBranch(
            @PathVariable UUID branchId) {

        return branchService.getBranch(branchId);
    }


    // Update branch information
    @PutMapping("/{branchId}")
    public BranchResponse updateBranch(
            @PathVariable UUID branchId,
            @Valid @RequestBody BranchRequest request) {

        return branchService.updateBranch(branchId, request);
    }


    // Activate or deactivate a branch
    @PatchMapping("/{branchId}/status")
    public BranchResponse updateBranchStatus(
            @PathVariable UUID branchId,
            @Valid @RequestBody BranchStatusRequest request) {

        return branchService.updateBranchStatus(
                branchId,
                request.active()
        );
    }
}
