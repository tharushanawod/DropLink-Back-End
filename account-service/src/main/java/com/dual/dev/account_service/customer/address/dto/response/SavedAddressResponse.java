package com.dual.dev.account_service.customer.address.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SavedAddressResponse(

        UUID id,

        String label,

        String recipientName,

        String recipientPhone,

        String addressLine1,

        String addressLine2,

        String city,

        String district,

        String postalCode,

        String country,

        BigDecimal latitude,

        BigDecimal longitude,

        boolean defaultAddress,

        Instant createdAt,

        Instant updatedAt

) {}