
package com.dual.dev.dispatch_service.dispatch.controller;

import com.dual.dev.dispatch_service.dispatch.dto.*;
import com.dual.dev.dispatch_service.dispatch.entity.*;
import com.dual.dev.dispatch_service.dispatch.enums.DispatchStatus;
import com.dual.dev.dispatch_service.dispatch.enums.DispatchType;
import com.dual.dev.dispatch_service.dispatch.service.DispatchService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dispatches")
@RequiredArgsConstructor
@Validated
public class DispatchController {

    private final DispatchService dispatchService;

    // 1. Create a new draft dispatch
    @PostMapping
    public ResponseEntity<DispatchResponse> createDispatch(
            @Valid @RequestBody CreateDispatchRequest request) {

        DispatchResponse response = dispatchService.createDispatch(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 2. Search/filter dispatches
    @GetMapping
    public ResponseEntity<Page<DispatchResponse>> getDispatches(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) DispatchType type,
            @RequestParam(required = false) DispatchStatus status,
            Pageable pageable) {

        return ResponseEntity.ok(
                dispatchService.getDispatches(
                        branchId, type, status, pageable)
        );
    }

    // 3. Get a dispatch and its details
    @GetMapping("/{id}")
    public ResponseEntity<DispatchResponse> getDispatch(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                dispatchService.getDispatch(id)
        );
    }

    // 4. Edit dispatch details (DRAFT only)
    @PatchMapping("/{id}")
    public ResponseEntity<DispatchResponse> updateDispatch(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDispatchRequest request) {

        return ResponseEntity.ok(
                dispatchService.updateDispatch(id, request)
        );
    }

    // 5. Add packages to a dispatch
    @PostMapping("/{id}/items")
    public ResponseEntity<DispatchResponse> addPackages(
            @PathVariable UUID id,
            @Valid @RequestBody AddDispatchItemsRequest request) {

        return ResponseEntity.ok(
                dispatchService.addPackages(id, request)
        );
    }

    // 6. Remove package (DRAFT only)
    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> removePackage(
            @PathVariable UUID id,
            @PathVariable UUID itemId) {

        dispatchService.removePackage(id, itemId);

        return ResponseEntity.noContent().build();
    }

    // 7. Assign route, driver and vehicle
    @PutMapping("/{id}/assignment")
    public ResponseEntity<DispatchResponse> assignResources(
            @PathVariable UUID id,
            @Valid @RequestBody AssignDispatchRequest request) {

        return ResponseEntity.ok(
                dispatchService.assignResources(id, request)
        );
    }

    // 8. Confirm physical package loading
    @PostMapping("/{id}/items/{itemId}/load")
    public ResponseEntity<DispatchResponse> loadPackage(
            @PathVariable UUID id,
            @PathVariable UUID itemId) {

        return ResponseEntity.ok(
                dispatchService.loadPackage(id, itemId)
        );
    }

    // 9. Confirm dispatch is ready
    @PostMapping("/{id}/ready")
    public ResponseEntity<DispatchResponse> markReady(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                dispatchService.markReady(id)
        );
    }

    // 10. Start dispatch journey
    @PostMapping("/{id}/depart")
    public ResponseEntity<DispatchResponse> depart(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                dispatchService.depart(id)
        );
    }

    // 11. Unload package at branch
    @PostMapping("/{id}/items/{itemId}/unload")
    public ResponseEntity<DispatchResponse> unloadPackage(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody HandoffRequest request) {

        return ResponseEntity.ok(
                dispatchService.unloadPackage(id, itemId, request)
        );
    }

    // 12. Confirm delivery to receiver
    @PostMapping("/{id}/items/{itemId}/deliver")
    public ResponseEntity<DispatchResponse> deliverPackage(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody HandoffRequest request) {

        return ResponseEntity.ok(
                dispatchService.deliverPackage(id, itemId, request)
        );
    }

    // 13. Record failed handoff
    @PostMapping("/{id}/items/{itemId}/fail")
    public ResponseEntity<DispatchResponse> failPackage(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody FailDispatchItemRequest request) {

        return ResponseEntity.ok(
                dispatchService.failPackage(id, itemId, request)
        );
    }

    // 14. Complete dispatch
    @PostMapping("/{id}/complete")
    public ResponseEntity<DispatchResponse> completeDispatch(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                dispatchService.completeDispatch(id)
        );
    }

    // 15. Cancel dispatch
    @PostMapping("/{id}/cancel")
    public ResponseEntity<DispatchResponse> cancelDispatch(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                dispatchService.cancelDispatch(id)
        );
    }

    // 16. Get packages eligible for a new dispatch
    @GetMapping("/eligible-packages")
    public ResponseEntity<List<EligiblePackageResponse>> getEligiblePackages(
            @RequestParam UUID branchId,
            @RequestParam DispatchType type) {

        return ResponseEntity.ok(
                dispatchService.getEligiblePackages(branchId, type)
        );
    }
}
