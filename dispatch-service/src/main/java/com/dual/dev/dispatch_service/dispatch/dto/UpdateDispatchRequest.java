package com.dual.dev.dispatch_service.dispatch.dto;

import java.time.LocalDateTime;

public record UpdateDispatchRequest(
        LocalDateTime scheduledDeparture
) {}