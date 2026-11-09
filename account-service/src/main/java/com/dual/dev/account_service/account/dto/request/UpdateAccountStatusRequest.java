package com.dual.dev.account_service.account.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateAccountStatusRequest(

        @NotNull(message = "Account status is required")
        Boolean active

) {}