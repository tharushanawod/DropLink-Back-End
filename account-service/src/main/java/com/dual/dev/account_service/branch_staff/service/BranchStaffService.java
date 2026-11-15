package com.dual.dev.account_service.branch_staff.service;

import com.dual.dev.account_service.account.entity.Account;
import com.dual.dev.account_service.account.repository.AccountRepository;

import com.dual.dev.account_service.branch.entity.Branch;
import com.dual.dev.account_service.branch.repository.BranchRepository;

import com.dual.dev.account_service.branch_staff.dto.request.BranchStaffAssignRequest;
import com.dual.dev.account_service.branch_staff.dto.request.BranchStaffUpdateRequest;
import com.dual.dev.account_service.branch_staff.dto.response.BranchStaffResponse;

import com.dual.dev.account_service.branch_staff.entity.BranchStaff;
import com.dual.dev.account_service.branch_staff.enums.BranchStaffDesignation;
import com.dual.dev.account_service.branch_staff.repository.BranchStaffRepository;

import com.dual.dev.account_service.common.exception.BadRequestException;
import com.dual.dev.account_service.common.exception.ConflictException;
import com.dual.dev.account_service.common.exception.ResourceNotFoundException;

import com.dual.dev.account_service.identity.KeycloakRoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BranchStaffService {

    private static final String BRANCH_STAFF_ROLE = "BRANCH_STAFF";

    private final BranchStaffRepository branchStaffRepository;

    private final BranchRepository branchRepository;

    private final AccountRepository accountRepository;

    private final KeycloakRoleService keycloakRoleService;


    // =========================================================
    // ADMINISTRATOR OPERATIONS
    // =========================================================


    // Assign an existing Account to a branch
    public BranchStaffResponse assignStaff(
            UUID branchId,
            BranchStaffAssignRequest request) {

        Branch branch = getActiveBranch(branchId);

        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found."
                        )
                );

        if (!account.isActive()) {
            throw new ConflictException(
                    "An inactive account cannot be assigned as branch staff."
            );
        }

        if (account.getKeycloakUserId() == null
                || account.getKeycloakUserId().isBlank()) {

            throw new BadRequestException(
                    "The account is not linked to a Keycloak user."
            );
        }

        // Each account can have only one branch staff assignment.
        if (branchStaffRepository.existsByAccount_Id(account.getId())) {

            throw new ConflictException(
                    "This account already has a branch staff assignment. "
                            + "Update or reactivate the existing assignment instead."
            );
        }

        ensureManagerAvailable(
                branchId,
                request.designation(),
                null
        );

        BranchStaff staff = new BranchStaff();

        staff.setAccount(account);
        staff.setBranch(branch);
        staff.setDesignation(request.designation());
        staff.setActive(true);
        staff.setAssignedAt(Instant.now());

        BranchStaff savedStaff = branchStaffRepository.save(staff);

        // Assign the Keycloak role to the existing user.
        keycloakRoleService.addRealmRole(
                account.getKeycloakUserId(),
                BRANCH_STAFF_ROLE
        );

        // If the database transaction rolls back after Keycloak succeeds,
        // remove the role to compensate for the external change.
        registerRollbackAction(
                () -> keycloakRoleService.removeRealmRole(
                        account.getKeycloakUserId(),
                        BRANCH_STAFF_ROLE
                ),
                "Could not compensate Keycloak role assignment."
        );

        return toResponse(savedStaff);
    }


    // Retrieve staff members belonging to a branch
    @Transactional(readOnly = true)
    public Page<BranchStaffResponse> getBranchStaff(
            UUID branchId,
            Boolean active,
            Pageable pageable) {

        getBranchEntity(branchId);

        Page<BranchStaff> staff;

        if (active == null) {

            staff = branchStaffRepository.findAllByBranch_Id(
                    branchId,
                    pageable
            );

        } else {

            staff = branchStaffRepository.findAllByBranch_IdAndActive(
                    branchId,
                    active,
                    pageable
            );
        }

        return staff.map(this::toResponse);
    }


    // Retrieve a particular staff assignment
    @Transactional(readOnly = true)
    public BranchStaffResponse getStaff(UUID staffId) {

        return toResponse(getStaffEntity(staffId));
    }


    // Transfer staff to another branch or change their designation
    public BranchStaffResponse updateStaff(
            UUID staffId,
            BranchStaffUpdateRequest request) {

        BranchStaff staff = getStaffEntity(staffId);

        if (!staff.isActive()) {
            throw new ConflictException(
                    "Activate the staff assignment before updating it."
            );
        }

        Branch branch = getActiveBranch(request.branchId());

        ensureManagerAvailable(
                branch.getId(),
                request.designation(),
                staff.getId()
        );

        staff.setBranch(branch);
        staff.setDesignation(request.designation());

        // Record the latest assignment/transfer time.
        staff.setAssignedAt(Instant.now());

        BranchStaff updatedStaff =
                branchStaffRepository.save(staff);

        // The account already has BRANCH_STAFF.
        // A branch transfer does not require another role assignment.

        return toResponse(updatedStaff);
    }


    // Deactivate a branch staff member
    public BranchStaffResponse deactivateStaff(UUID staffId) {

        BranchStaff staff = getStaffEntity(staffId);

        if (!staff.isActive()) {
            throw new ConflictException(
                    "This branch staff assignment is already inactive."
            );
        }

        staff.setActive(false);
        staff.setDeactivatedAt(Instant.now());

        BranchStaff updatedStaff =
                branchStaffRepository.save(staff);

        // Remove BRANCH_STAFF from Keycloak.
        keycloakRoleService.removeRealmRole(
                staff.getAccount().getKeycloakUserId(),
                BRANCH_STAFF_ROLE
        );

        // Restore the role if the PostgreSQL transaction rolls back.
        registerRollbackAction(
                () -> keycloakRoleService.addRealmRole(
                        staff.getAccount().getKeycloakUserId(),
                        BRANCH_STAFF_ROLE
                ),
                "Could not restore the Keycloak branch staff role."
        );

        return toResponse(updatedStaff);
    }


    // Reactivate an inactive branch staff member
    public BranchStaffResponse activateStaff(UUID staffId) {

        BranchStaff staff = getStaffEntity(staffId);

        if (staff.isActive()) {
            throw new ConflictException(
                    "This branch staff assignment is already active."
            );
        }

        if (!staff.getAccount().isActive()) {
            throw new ConflictException(
                    "An inactive account cannot be activated as branch staff."
            );
        }

        if (!staff.getBranch().isActive()) {
            throw new ConflictException(
                    "Staff cannot be assigned to an inactive branch."
            );
        }

        ensureManagerAvailable(
                staff.getBranch().getId(),
                staff.getDesignation(),
                staff.getId()
        );

        staff.setActive(true);
        staff.setAssignedAt(Instant.now());
        staff.setDeactivatedAt(null);

        BranchStaff activatedStaff =
                branchStaffRepository.save(staff);

        // Restore the Keycloak role.
        keycloakRoleService.addRealmRole(
                staff.getAccount().getKeycloakUserId(),
                BRANCH_STAFF_ROLE
        );

        // If the database update fails, undo the role assignment.
        registerRollbackAction(
                () -> keycloakRoleService.removeRealmRole(
                        staff.getAccount().getKeycloakUserId(),
                        BRANCH_STAFF_ROLE
                ),
                "Could not compensate Keycloak role reactivation."
        );

        return toResponse(activatedStaff);
    }


    // =========================================================
    // BRANCH STAFF SELF-SERVICE
    // =========================================================


    // Retrieve the authenticated employee's own branch assignment
    @Transactional(readOnly = true)
    public BranchStaffResponse getMyBranchStaffProfile(
            String keycloakUserId) {

        BranchStaff staff = branchStaffRepository
                .findByAccount_KeycloakUserId(keycloakUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Branch staff assignment not found."
                        )
                );

        if (!staff.isActive()
                || !staff.getBranch().isActive()
                || !staff.getAccount().isActive()) {

            throw new AccessDeniedException(
                    "Your branch staff assignment is inactive."
            );
        }

        return toResponse(staff);
    }


    // =========================================================
    // PRIVATE HELPER METHODS
    // =========================================================


    private Branch getBranchEntity(UUID branchId) {

        return branchRepository.findById(branchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Branch not found."
                        )
                );
    }


    private Branch getActiveBranch(UUID branchId) {

        Branch branch = getBranchEntity(branchId);

        if (!branch.isActive()) {
            throw new ConflictException(
                    "Staff cannot be assigned to an inactive branch."
            );
        }

        return branch;
    }


    private BranchStaff getStaffEntity(UUID staffId) {

        return branchStaffRepository.findById(staffId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Branch staff assignment not found."
                        )
                );
    }


    // Ensure that only one active branch manager exists per branch.
    private void ensureManagerAvailable(
            UUID branchId,
            BranchStaffDesignation designation,
            UUID currentStaffId) {

        if (designation != BranchStaffDesignation.BRANCH_MANAGER) {
            return;
        }

        boolean managerExists;

        if (currentStaffId == null) {

            managerExists =
                    branchStaffRepository
                            .existsByBranch_IdAndDesignationAndActiveTrue(
                                    branchId,
                                    BranchStaffDesignation.BRANCH_MANAGER
                            );

        } else {

            managerExists =
                    branchStaffRepository
                            .existsByBranch_IdAndDesignationAndActiveTrueAndIdNot(
                                    branchId,
                                    BranchStaffDesignation.BRANCH_MANAGER,
                                    currentStaffId
                            );
        }

        if (managerExists) {
            throw new ConflictException(
                    "This branch already has an active branch manager."
            );
        }
    }


    // Register a compensating action for an external Keycloak operation.
    private void registerRollbackAction(
            Runnable compensation,
            String errorMessage) {

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException(
                    "No active database transaction is available."
            );
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCompletion(int status) {

                        if (status != STATUS_COMMITTED) {

                            try {
                                compensation.run();

                            } catch (RuntimeException exception) {

                                log.error(
                                        errorMessage,
                                        exception
                                );
                            }
                        }
                    }
                }
        );
    }


    // Convert BranchStaff entity into response DTO
    private BranchStaffResponse toResponse(BranchStaff staff) {

        Account account = staff.getAccount();

        Branch branch = staff.getBranch();

        return new BranchStaffResponse(

                staff.getId(),

                account.getId(),

                account.getEmail(),

                account.getFirstName(),

                account.getLastName(),

                account.getPhoneNumber(),

                branch.getId(),

                branch.getBranchCode(),

                branch.getName(),

                staff.getDesignation(),

                staff.isActive(),

                staff.getAssignedAt(),

                staff.getDeactivatedAt(),

                staff.getCreatedAt(),

                staff.getUpdatedAt()
        );
    }
}
