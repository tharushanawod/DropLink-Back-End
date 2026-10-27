package com.dual.dev.dispatch_service.dispatch.repository;

import com.dual.dev.dispatch_service.dispatch.entity.DispatchItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DispatchItemRepository
        extends JpaRepository<DispatchItem, UUID> {

    List<DispatchItem> findByDispatchId(UUID dispatchId);

    boolean existsByDispatchIdAndPackageId(
            UUID dispatchId,
            UUID packageId
    );
}