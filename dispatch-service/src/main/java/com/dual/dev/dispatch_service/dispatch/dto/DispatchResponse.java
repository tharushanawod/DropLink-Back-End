package com.dual.dev.dispatch_service.dispatch.dto;

import com.dual.dev.dispatch_service.dispatch.enums.DispatchCreationMode;
import com.dual.dev.dispatch_service.dispatch.enums.DispatchStatus;
import com.dual.dev.dispatch_service.dispatch.enums.DispatchType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DispatchResponse(
        UUID id,
        String dispatchNumber,
        DispatchType type,
        DispatchStatus status,
        DispatchCreationMode creationMode,
        UUID originBranchId,
        UUID routeId,
        UUID driverId,
        UUID vehicleId,
        LocalDateTime scheduledDeparture,
        LocalDateTime actualDeparture,
        LocalDateTime completedAt,
        List<DispatchItemResponse> items
) {}