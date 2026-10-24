package com.dual.dev.shipment_service.shipment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record  UpdateShipmentRequest (
    @Size(max = 100)
    String senderName,

    @Pattern(
            regexp = "^0[0-9]{9}$",
            message = "Enter a valid Sri Lankan phone number"
    )
    String senderPhone,

    @Size(max = 100)
    String receiverName,

    @Pattern(
            regexp = "^0[0-9]{9}$",
            message = "Enter a valid Sri Lankan phone number"
    )
    String receiverPhone,

    String pickupAddress,

    String deliveryAddress,

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    BigDecimal pickupLatitude,

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    BigDecimal pickupLongitude,

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    BigDecimal deliveryLatitude,

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    BigDecimal deliveryLongitude,

    @Valid
    List<UpdatePackageRequest> packages
){

}
