package com.dual.dev.dispatch_service.dispatch.dto;

import com.dual.dev.dispatch_service.dispatch.enums.DispatchType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateDispatchRequest(
        @NotNull UUID originBranchId,
        @NotNull DispatchType type,
        LocalDateTime scheduledDeparture
) {}
