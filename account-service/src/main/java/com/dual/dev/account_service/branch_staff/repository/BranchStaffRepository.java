package com.dual.dev.account_service.branch_staff.repository;

import com.dual.dev.account_service.branch_staff.entity.BranchStaff;
import com.dual.dev.account_service.branch_staff.enums.BranchStaffDesignation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BranchStaffRepository
        extends JpaRepository<BranchStaff, UUID> {


    // Find a staff assignment using an Account ID
    Optional<BranchStaff> findByAccount_Id(UUID accountId);


    // Find a staff assignment using the Keycloak user ID
    Optional<BranchStaff> findByAccount_KeycloakUserId(String keycloakUserId);


    // Check whether an account already has a staff assignment
    boolean existsByAccount_Id(UUID accountId);


    // Retrieve all staff members of a branch
    Page<BranchStaff> findAllByBranch_Id(
            UUID branchId,
            Pageable pageable
    );


    // Retrieve active/inactive staff members of a branch
    Page<BranchStaff> findAllByBranch_IdAndActive(
            UUID branchId,
            boolean active,
            Pageable pageable
    );


    // Check whether a branch has any active staff members
    boolean existsByBranch_IdAndActiveTrue(UUID branchId);


    // Check whether a branch already has an active manager
    boolean existsByBranch_IdAndDesignationAndActiveTrue(
            UUID branchId,
            BranchStaffDesignation designation
    );


    // Check whether another active manager exists, excluding a staff record
    boolean existsByBranch_IdAndDesignationAndActiveTrueAndIdNot(
            UUID branchId,
            BranchStaffDesignation designation,
            UUID staffId
    );


    // Verify that an account has an active assignment to an active branch
    boolean existsByAccount_KeycloakUserIdAndActiveTrueAndBranch_ActiveTrue(
            String keycloakUserId
    );
}
