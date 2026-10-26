package com.dual.dev.dispatch_service.dispatch.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AddDispatchItemsRequest(
        @NotEmpty List<@NotNull UUID> packageIds
) {}