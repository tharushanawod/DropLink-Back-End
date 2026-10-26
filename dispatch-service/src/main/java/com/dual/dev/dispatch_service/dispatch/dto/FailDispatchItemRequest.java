package com.dual.dev.dispatch_service.dispatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FailDispatchItemRequest(
        @NotBlank String reason,
        @NotNull UUID stopId
) {}