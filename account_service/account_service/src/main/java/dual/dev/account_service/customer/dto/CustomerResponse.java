package com.dual.dev.account_service.customer.dto;

import com.dual.dev.account_service.customer.enums.CustomerStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(

        UUID id,

        String firstName,

        String lastName,

        String phone,

        CustomerStatus status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {}