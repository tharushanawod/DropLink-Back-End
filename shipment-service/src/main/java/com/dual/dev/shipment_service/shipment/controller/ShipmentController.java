package com.dual.dev.shipment_service.shipment.controller;

import com.dual.dev.shipment_service.shipment.dto.CreateShipmentRequest;
import com.dual.dev.shipment_service.shipment.dto.UpdateShipmentRequest;
import com.dual.dev.shipment_service.shipment.dto.ShipmentResponse;
import com.dual.dev.shipment_service.shipment.service.ShipmentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    // 1. Create shipment
    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(
            @Valid @RequestBody CreateShipmentRequest request
    ) {
        ShipmentResponse response =
                shipmentService.createShipment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 2. Get shipment by ID
    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getShipmentById(
            @PathVariable UUID id
    ) {
        ShipmentResponse response =
                shipmentService.getShipmentById(id);

        return ResponseEntity.ok(response);
    }

    // 3. Get shipment by tracking number
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> getShipmentByTrackingNumber(
            @PathVariable String trackingNumber
    ) {
        ShipmentResponse response =
                shipmentService.getShipmentByTrackingNumber(trackingNumber);

        return ResponseEntity.ok(response);
    }

    // 4. Update shipment
    @PatchMapping("/{id}")
    public ResponseEntity<ShipmentResponse> updateShipment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateShipmentRequest request
    ) {
        ShipmentResponse response =
                shipmentService.updateShipment(id, request);

        return ResponseEntity.ok(response);
    }
}