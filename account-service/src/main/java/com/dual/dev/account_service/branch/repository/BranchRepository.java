package com.dual.dev.account_service.branch.repository;

import com.dual.dev.account_service.branch.entity.Branch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    boolean existsByBranchCodeIgnoreCase(String branchCode);

    boolean existsByBranchCodeIgnoreCaseAndIdNot(
            String branchCode,
            UUID id
    );

    Page<Branch> findAllByActive(
            boolean active,
            Pageable pageable
    );

}
