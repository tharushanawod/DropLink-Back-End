package com.dual.dev.shipment_service.shipment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateShipmentRequest(

        @NotBlank(message = "Sender name is required")
        @Size(max = 100)
        String senderName,

        @NotBlank(message = "Sender phone is required")
        @Pattern(
                regexp = "^0[0-9]{9}$",
                message = "Enter a valid Sri Lankan phone number"
        )
        String senderPhone,

        @NotBlank(message = "Receiver name is required")
        @Size(max = 100)
        String receiverName,

        @NotBlank(message = "Receiver phone is required")
        @Pattern(
                regexp = "^0[0-9]{9}$",
                message = "Enter a valid Sri Lankan phone number"
        )
        String receiverPhone,

        @NotBlank(message = "Pickup address is required")
        String pickupAddress,

        @NotBlank(message = "Delivery address is required")
        String deliveryAddress,

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        BigDecimal pickupLatitude,

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        BigDecimal pickupLongitude,

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        BigDecimal deliveryLatitude,

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        BigDecimal deliveryLongitude,

        @NotNull
        UUID deliveryBranchId,

        @NotEmpty(message = "At least one package is required")
        @Valid
        List<CreatePackageRequest> packages
) {}
