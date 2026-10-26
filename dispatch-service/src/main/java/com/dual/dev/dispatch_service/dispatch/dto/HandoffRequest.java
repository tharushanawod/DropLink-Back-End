package com.dual.dev.dispatch_service.dispatch.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HandoffRequest(
        @NotNull UUID stopId
) {}