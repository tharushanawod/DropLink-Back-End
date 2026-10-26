package com.dual.dev.dispatch_service.dispatch.dto;

import com.dual.dev.dispatch_service.dispatch.enums.DispatchItemStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DispatchItemResponse(
        UUID id,
        UUID packageId,
        UUID pickupStopId,
        UUID dropoffStopId,
        DispatchItemStatus status,
        LocalDateTime loadedAt,
        LocalDateTime unloadedAt
) {}