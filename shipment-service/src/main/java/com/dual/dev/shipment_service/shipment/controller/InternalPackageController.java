package com.dual.dev.shipment_service.shipment.controller;

import com.dual.dev.shipment_service.shipment.dto.EligiblePackageResponse;
import com.dual.dev.shipment_service.shipment.enums.DispatchType;
import com.dual.dev.shipment_service.shipment.service.PackageQueryService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/packages")
@RequiredArgsConstructor
public class InternalPackageController {

    private final PackageQueryService packageQueryService;

    @GetMapping("/eligible")
    public ResponseEntity<List<EligiblePackageResponse>> getEligiblePackages(
            @RequestParam UUID branchId,
            @RequestParam DispatchType type) {

        return ResponseEntity.ok(
                packageQueryService.getEligiblePackages(branchId, type)
        );
    }
}