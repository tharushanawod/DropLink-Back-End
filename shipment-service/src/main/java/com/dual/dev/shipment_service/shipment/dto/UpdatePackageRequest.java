
package com.dual.dev.shipment_service.shipment.dto;

import com.dual.dev.shipment_service.shipment.enums.PackageStatus;
import com.dual.dev.shipment_service.shipment.enums.PackageType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UpdatePackageRequest(

        UUID id,

        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal weightKg,

        @Positive
        BigDecimal lengthCm,

        @Positive
        BigDecimal widthCm,

        @Positive
        BigDecimal heightCm,

        PackageType packageType,

        @DecimalMin("0.0")
        BigDecimal declaredValue,

        @Size(max = 500)
        String description,

        UUID currentBranchId,

        PackageStatus currentStatus,

        LocalDateTime deliveredAt

) {}
