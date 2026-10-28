package com.dual.dev.shipment_service.shipment.dto;

import com.dual.dev.shipment_service.shipment.enums.PackageStatus;
import com.dual.dev.shipment_service.shipment.enums.PackageType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreatePackageRequest(

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal weightKg,

        @Positive
        BigDecimal lengthCm,

        @Positive
        BigDecimal widthCm,

        @Positive
        BigDecimal heightCm,

        @NotNull
        PackageType packageType,

        @DecimalMin("0.0")
        BigDecimal declaredValue,

        @Size(max = 500)
        String description,

        UUID currentBranchId,

        @NotNull
        PackageStatus currentStatus,

        LocalDateTime deliveredAt
) {}
