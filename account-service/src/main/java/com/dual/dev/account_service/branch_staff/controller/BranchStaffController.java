package com.dual.dev.account_service.branch_staff.controller;

import com.dual.dev.account_service.branch_staff.dto.response.BranchStaffResponse;
import com.dual.dev.account_service.branch_staff.service.BranchStaffService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/branch-staff")
@RequiredArgsConstructor
@PreAuthorize(
        "hasRole('BRANCH_STAFF') "
                + "and @accountStatusChecker.isActive(authentication) "
                + "and @branchStaffAccessChecker.isActive(authentication)"
)
public class BranchStaffController {

    private final BranchStaffService branchStaffService;


    // Retrieve the authenticated branch staff member's own assignment
    @GetMapping("/me")
    public BranchStaffResponse getMyProfile(
            @AuthenticationPrincipal Jwt jwt) {

        return branchStaffService.getMyBranchStaffProfile(
                jwt.getSubject()
        );
    }
}
