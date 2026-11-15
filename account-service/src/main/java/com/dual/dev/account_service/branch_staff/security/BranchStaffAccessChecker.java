package com.dual.dev.account_service.branch_staff.security;

import com.dual.dev.account_service.branch_staff.repository.BranchStaffRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.stereotype.Component;

@Component("branchStaffAccessChecker")
@RequiredArgsConstructor
public class BranchStaffAccessChecker {

    private final BranchStaffRepository branchStaffRepository;


    public boolean isActive(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return false;
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        String keycloakUserId = jwt.getSubject();

        return branchStaffRepository
                .existsByAccount_KeycloakUserIdAndActiveTrueAndBranch_ActiveTrue(
                        keycloakUserId
                );
    }
}
