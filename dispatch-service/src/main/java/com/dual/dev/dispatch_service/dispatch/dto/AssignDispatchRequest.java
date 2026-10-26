package com.dual.dev.dispatch_service.dispatch.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignDispatchRequest(
        @NotNull UUID routeId,
        @NotNull UUID driverId,
        @NotNull UUID vehicleId
) {}