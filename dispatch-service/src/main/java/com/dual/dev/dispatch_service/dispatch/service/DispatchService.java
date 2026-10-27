
package com.dual.dev.dispatch_service.dispatch.service;

import com.dual.dev.dispatch_service.dispatch.dto.*;
import com.dual.dev.dispatch_service.dispatch.entity.*;
import com.dual.dev.dispatch_service.dispatch.enums.DispatchCreationMode;
import com.dual.dev.dispatch_service.dispatch.enums.DispatchItemStatus;
import com.dual.dev.dispatch_service.dispatch.enums.DispatchStatus;
import com.dual.dev.dispatch_service.dispatch.enums.DispatchType;
import com.dual.dev.dispatch_service.dispatch.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DispatchService {

    private final DispatchRepository dispatchRepository;
    private final DispatchItemRepository dispatchItemRepository;

    // 1. CREATE DISPATCH
    @Transactional
    public DispatchResponse createDispatch(
            CreateDispatchRequest request) {

        Dispatch dispatch = Dispatch.builder()
                .dispatchNumber(generateDispatchNumber())
                .originBranchId(request.originBranchId())
                .type(request.type())
                .scheduledDeparture(request.scheduledDeparture())
                .status(DispatchStatus.DRAFT)
                .creationMode(DispatchCreationMode.MANUAL)
                .build();

        return toResponse(dispatchRepository.save(dispatch));
    }

    // 2. GET ALL DISPATCHES WITH FILTERS
    @Transactional(readOnly = true)
    public Page<DispatchResponse> getDispatches(
            UUID branchId,
            DispatchType type,
            DispatchStatus status,
            Pageable pageable) {

        Specification<Dispatch> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (branchId != null) {
                predicates.add(cb.equal(
                        root.get("originBranchId"), branchId));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return dispatchRepository.findAll(spec, pageable)
                .map(this::toResponse);
    }

    // 3. GET DISPATCH BY ID
    @Transactional(readOnly = true)
    public DispatchResponse getDispatch(UUID id) {
        return toResponse(findDispatch(id));
    }

    // 4. UPDATE DRAFT DISPATCH
    @Transactional
    public DispatchResponse updateDispatch(
            UUID id, UpdateDispatchRequest request) {

        Dispatch dispatch = findDispatch(id);
        requireDraft(dispatch);

        if (request.scheduledDeparture() != null) {
            dispatch.setScheduledDeparture(
                    request.scheduledDeparture());
        }

        return toResponse(dispatchRepository.save(dispatch));
    }

    // 5. ADD PACKAGES
    @Transactional
    public DispatchResponse addPackages(
            UUID id, AddDispatchItemsRequest request) {

        Dispatch dispatch = findDispatch(id);
        requireDraft(dispatch);

        Set<UUID> packageIds =
                new HashSet<>(request.packageIds());

        if (packageIds.size() != request.packageIds().size()) {
            throw badRequest("Duplicate package IDs in request");
        }

        for (UUID packageId : packageIds) {

            if (dispatchItemRepository
                    .existsByDispatchIdAndPackageId(
                            id, packageId)) {
                throw conflict(
                        "Package already belongs to this dispatch");
            }

            DispatchItem item = DispatchItem.builder()
                    .dispatch(dispatch)
                    .packageId(packageId)
                    .status(DispatchItemStatus.ASSIGNED)
                    .build();

            dispatchItemRepository.save(item);
        }

        return toResponse(dispatch);
    }

    // 6. REMOVE PACKAGE
    @Transactional
    public void removePackage(UUID id, UUID itemId) {

        Dispatch dispatch = findDispatch(id);
        requireDraft(dispatch);

        DispatchItem item = findItem(id, itemId);

        if (item.getStatus() != DispatchItemStatus.ASSIGNED) {
            throw conflict("Only unprocessed items can be removed");
        }

        dispatchItemRepository.delete(item);
    }

    // 7. ASSIGN ROUTE, DRIVER AND VEHICLE
    @Transactional
    public DispatchResponse assignResources(
            UUID id, AssignDispatchRequest request) {

        Dispatch dispatch = findDispatch(id);
        requireDraft(dispatch);

        dispatch.setRouteId(request.routeId());
        dispatch.setDriverId(request.driverId());
        dispatch.setVehicleId(request.vehicleId());

        return toResponse(dispatchRepository.save(dispatch));
    }

    // 8. LOAD PACKAGE
    @Transactional
    public DispatchResponse loadPackage(UUID id, UUID itemId) {

        Dispatch dispatch = findDispatch(id);
        requireDraft(dispatch);

        DispatchItem item = findItem(id, itemId);

        requireItemStatus(item, DispatchItemStatus.ASSIGNED);

        item.setStatus(DispatchItemStatus.LOADED);
        item.setLoadedAt(LocalDateTime.now());

        dispatchItemRepository.save(item);

        return toResponse(dispatch);
    }

    // 9. MARK DISPATCH READY
    @Transactional
    public DispatchResponse markReady(UUID id) {

        Dispatch dispatch = findDispatch(id);
        requireDraft(dispatch);

        if (dispatch.getRouteId() == null ||
                dispatch.getDriverId() == null ||
                dispatch.getVehicleId() == null) {
            throw conflict(
                    "Route, driver and vehicle must be assigned");
        }

        List<DispatchItem> items =
                dispatchItemRepository.findByDispatchId(id);

        if (items.isEmpty()) {
            throw conflict("Dispatch has no packages");
        }

        boolean allLoaded = items.stream().allMatch(
                item -> item.getStatus() ==
                        DispatchItemStatus.LOADED);

        if (!allLoaded) {
            throw conflict(
                    "All packages must be loaded before marking ready");
        }

        dispatch.setStatus(DispatchStatus.READY);

        return toResponse(dispatchRepository.save(dispatch));
    }

    // 10. DEPART
    @Transactional
    public DispatchResponse depart(UUID id) {

        Dispatch dispatch = findDispatch(id);

        if (dispatch.getStatus() != DispatchStatus.READY) {
            throw conflict("Dispatch must be READY to depart");
        }

        dispatch.setStatus(DispatchStatus.IN_TRANSIT);
        dispatch.setActualDeparture(LocalDateTime.now());

        return toResponse(dispatchRepository.save(dispatch));
    }

    // 11. UNLOAD PACKAGE AT BRANCH
    @Transactional
    public DispatchResponse unloadPackage(
            UUID id, UUID itemId, HandoffRequest request) {

        Dispatch dispatch = findDispatch(id);
        requireInTransit(dispatch);

        if (dispatch.getType() != DispatchType.BRANCH_TRANSFER) {
            throw conflict(
                    "Unload operation requires BRANCH_TRANSFER");
        }

        DispatchItem item = findItem(id, itemId);
        requireItemStatus(item, DispatchItemStatus.LOADED);

        if (!Objects.equals(
                item.getDropoffStopId(), request.stopId())) {
            throw conflict("Incorrect drop-off stop");
        }

        item.setStatus(DispatchItemStatus.UNLOADED);
        item.setUnloadedAt(LocalDateTime.now());

        dispatchItemRepository.save(item);

        return toResponse(dispatch);
    }

    // 12. DELIVER PACKAGE TO RECEIVER
    @Transactional
    public DispatchResponse deliverPackage(
            UUID id, UUID itemId, HandoffRequest request) {

        Dispatch dispatch = findDispatch(id);
        requireInTransit(dispatch);

        if (dispatch.getType() != DispatchType.LAST_MILE_DELIVERY) {
            throw conflict(
                    "Delivery requires LAST_MILE_DELIVERY");
        }

        DispatchItem item = findItem(id, itemId);
        requireItemStatus(item, DispatchItemStatus.LOADED);

        if (!Objects.equals(
                item.getDropoffStopId(), request.stopId())) {
            throw conflict("Incorrect delivery stop");
        }

        item.setStatus(DispatchItemStatus.DELIVERED);
        item.setUnloadedAt(LocalDateTime.now());

        dispatchItemRepository.save(item);

        return toResponse(dispatch);
    }

    // 13. RECORD FAILED HANDOFF
    @Transactional
    public DispatchResponse failPackage(
            UUID id, UUID itemId,
            FailDispatchItemRequest request) {

        Dispatch dispatch = findDispatch(id);
        requireInTransit(dispatch);

        DispatchItem item = findItem(id, itemId);
        requireItemStatus(item, DispatchItemStatus.LOADED);

        item.setStatus(DispatchItemStatus.FAILED);

        dispatchItemRepository.save(item);

        // Persist failure reason and stop in a DispatchEvent
        // when event history is added.

        return toResponse(dispatch);
    }

    // 14. COMPLETE DISPATCH
    @Transactional
    public DispatchResponse completeDispatch(UUID id) {

        Dispatch dispatch = findDispatch(id);
        requireInTransit(dispatch);

        List<DispatchItem> items =
                dispatchItemRepository.findByDispatchId(id);

        if (items.isEmpty()) {
            throw conflict("Dispatch has no packages");
        }

        boolean allProcessed = items.stream().allMatch(
                item -> item.getStatus() ==
                        DispatchItemStatus.UNLOADED ||
                        item.getStatus() ==
                                DispatchItemStatus.DELIVERED ||
                        item.getStatus() ==
                                DispatchItemStatus.FAILED);

        if (!allProcessed) {
            throw conflict(
                    "All packages must have a final outcome");
        }

        dispatch.setStatus(DispatchStatus.COMPLETED);
        dispatch.setCompletedAt(LocalDateTime.now());

        return toResponse(dispatchRepository.save(dispatch));
    }

    // 15. CANCEL DISPATCH
    @Transactional
    public DispatchResponse cancelDispatch(UUID id) {

        Dispatch dispatch = findDispatch(id);

        if (dispatch.getStatus() != DispatchStatus.DRAFT &&
                dispatch.getStatus() != DispatchStatus.READY) {
            throw conflict(
                    "Only DRAFT or READY dispatches can be cancelled");
        }

        dispatch.setStatus(DispatchStatus.CANCELLED);

        return toResponse(dispatchRepository.save(dispatch));
    }

    // ---------------- HELPERS ----------------

    private Dispatch findDispatch(UUID id) {
        return dispatchRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Dispatch not found"));
    }

    private DispatchItem findItem(UUID dispatchId, UUID itemId) {

        DispatchItem item = dispatchItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Dispatch item not found"));

        if (!item.getDispatch().getId().equals(dispatchId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Item does not belong to this dispatch");
        }

        return item;
    }

    private void requireDraft(Dispatch dispatch) {
        if (dispatch.getStatus() != DispatchStatus.DRAFT) {
            throw conflict("Dispatch must be in DRAFT status");
        }
    }

    private void requireInTransit(Dispatch dispatch) {
        if (dispatch.getStatus() != DispatchStatus.IN_TRANSIT) {
            throw conflict("Dispatch must be IN_TRANSIT");
        }
    }

    private void requireItemStatus(
            DispatchItem item, DispatchItemStatus expected) {

        if (item.getStatus() != expected) {
            throw conflict("Expected item status: " + expected);
        }
    }

    private String generateDispatchNumber() {
        return "DSP-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(
                HttpStatus.CONFLICT, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST, message);
    }

    private DispatchResponse toResponse(Dispatch dispatch) {

        List<DispatchItemResponse> items =
                dispatchItemRepository.findByDispatchId(
                                dispatch.getId())
                        .stream()
                        .map(item -> new DispatchItemResponse(
                                item.getId(),
                                item.getPackageId(),
                                item.getPickupStopId(),
                                item.getDropoffStopId(),
                                item.getStatus(),
                                item.getLoadedAt(),
                                item.getUnloadedAt()
                        ))
                        .toList();

        return new DispatchResponse(
                dispatch.getId(),
                dispatch.getDispatchNumber(),
                dispatch.getType(),
                dispatch.getStatus(),
                dispatch.getCreationMode(),
                dispatch.getOriginBranchId(),
                dispatch.getRouteId(),
                dispatch.getDriverId(),
                dispatch.getVehicleId(),
                dispatch.getScheduledDeparture(),
                dispatch.getActualDeparture(),
                dispatch.getCompletedAt(),
                items
        );
    }
}
