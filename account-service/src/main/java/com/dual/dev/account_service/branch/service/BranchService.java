package com.dual.dev.account_service.branch.service;

import com.dual.dev.account_service.branch.dto.request.BranchRequest;
import com.dual.dev.account_service.branch.dto.response.BranchResponse;
import com.dual.dev.account_service.branch.entity.Branch;
import com.dual.dev.account_service.branch.repository.BranchRepository;

import com.dual.dev.account_service.branch_staff.repository.BranchStaffRepository;

import com.dual.dev.account_service.common.exception.BadRequestException;
import com.dual.dev.account_service.common.exception.ConflictException;
import com.dual.dev.account_service.common.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    private final BranchStaffRepository branchStaffRepository;


    // Create a new branch
    public BranchResponse createBranch(BranchRequest request) {

        String branchCode = normalizeBranchCode(request.branchCode());

        if (branchRepository.existsByBranchCodeIgnoreCase(branchCode)) {

            throw new ConflictException(
                    "A branch with this branch code already exists."
            );
        }

        Branch branch = new Branch();

        applyBranchInformation(branch, request, branchCode);

        branch.setActive(true);

        Branch savedBranch = branchRepository.save(branch);

        return toResponse(savedBranch);
    }


    // Retrieve branches, optionally filtering by active status
    @Transactional(readOnly = true)
    public Page<BranchResponse> getBranches(
            Boolean active,
            Pageable pageable) {

        Page<Branch> branches;

        if (active == null) {
            branches = branchRepository.findAll(pageable);
        } else {
            branches = branchRepository.findAllByActive(active, pageable);
        }

        return branches.map(this::toResponse);
    }


    // Retrieve one branch
    @Transactional(readOnly = true)
    public BranchResponse getBranch(UUID branchId) {

        return toResponse(getBranchEntity(branchId));
    }


    // Update branch information
    public BranchResponse updateBranch(
            UUID branchId,
            BranchRequest request) {

        Branch branch = getBranchEntity(branchId);

        String branchCode = normalizeBranchCode(request.branchCode());

        if (branchRepository.existsByBranchCodeIgnoreCaseAndIdNot(
                branchCode,
                branchId)) {

            throw new ConflictException(
                    "Another branch already uses this branch code."
            );
        }

        applyBranchInformation(branch, request, branchCode);

        Branch updatedBranch = branchRepository.save(branch);

        return toResponse(updatedBranch);
    }


    // Activate or deactivate a branch
    public BranchResponse updateBranchStatus(
            UUID branchId,
            boolean active) {

        Branch branch = getBranchEntity(branchId);

        if (!active
                && branchStaffRepository.existsByBranch_IdAndActiveTrue(branchId)) {

            throw new ConflictException(
                    "Deactivate or transfer the active branch staff before deactivating this branch."
            );
        }

        branch.setActive(active);

        Branch updatedBranch = branchRepository.save(branch);

        return toResponse(updatedBranch);
    }


    // =========================================================
    // PRIVATE HELPERS
    // =========================================================

    private Branch getBranchEntity(UUID branchId) {

        return branchRepository.findById(branchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Branch not found."
                        )
                );
    }


    private void applyBranchInformation(
            Branch branch,
            BranchRequest request,
            String branchCode) {

        branch.setBranchCode(branchCode);

        branch.setName(request.name().trim());

        branch.setAddressLine1(request.addressLine1().trim());

        branch.setAddressLine2(normalizeOptional(request.addressLine2()));

        branch.setCity(request.city().trim());

        branch.setDistrict(normalizeOptional(request.district()));

        branch.setPostalCode(normalizeOptional(request.postalCode()));

        branch.setCountry(request.country().trim());

        branch.setContactPhone(normalizeOptional(request.contactPhone()));

        branch.setContactEmail(normalizeOptional(request.contactEmail()));
    }


    private String normalizeBranchCode(String branchCode) {

        if (branchCode == null || branchCode.isBlank()) {
            throw new BadRequestException("Branch code is required.");
        }

        return branchCode.trim().toUpperCase(Locale.ROOT);
    }


    private String normalizeOptional(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }


    private BranchResponse toResponse(Branch branch) {

        return new BranchResponse(
                branch.getId(),
                branch.getBranchCode(),
                branch.getName(),
                branch.getAddressLine1(),
                branch.getAddressLine2(),
                branch.getCity(),
                branch.getDistrict(),
                branch.getPostalCode(),
                branch.getCountry(),
                branch.getContactPhone(),
                branch.getContactEmail(),
                branch.isActive(),
                branch.getCreatedAt(),
                branch.getUpdatedAt()
        );
    }
}
