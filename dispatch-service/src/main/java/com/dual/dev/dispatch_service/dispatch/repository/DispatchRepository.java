package com.dual.dev.dispatch_service.dispatch.repository;

import com.dual.dev.dispatch_service.dispatch.entity.Dispatch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface DispatchRepository
        extends JpaRepository<Dispatch, UUID>,
        JpaSpecificationExecutor<Dispatch> {
}