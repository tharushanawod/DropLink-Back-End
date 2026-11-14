package com.dual.dev.account_service.driver.profile.controller;

import com.dual.dev.account_service.common.dto.PageResponse;

import com.dual.dev.account_service.driver.profile.dto.request.RejectDriverRequest;
import com.dual.dev.account_service.driver.profile.dto.response.DriverResponse;

import com.dual.dev.account_service.driver.profile.service.DriverService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/drivers")
@PreAuthorize("hasRole('ADMIN') and @accountStatusChecker.isActive(authentication)")
public class DriverAdminController {

    private final DriverService driverService;

    public DriverAdminController(DriverService driverService) {
        this.driverService = driverService;
    }


    // Retrieve all drivers, optionally filtered by verification status
    @GetMapping
    public PageResponse<DriverResponse> getDrivers(

            @RequestParam(required = false)
            String status,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        Page<DriverResponse> driverPage =
                driverService.getDrivers(status, pageable);

        return new PageResponse<>(
                driverPage.getContent(),
                driverPage.getNumber(),
                driverPage.getSize(),
                driverPage.getTotalElements(),
                driverPage.getTotalPages(),
                driverPage.isFirst(),
                driverPage.isLast()
        );
    }


    // Retrieve one driver's profile
    @GetMapping("/{driverId}")
    public DriverResponse getDriver(
            @PathVariable UUID driverId) {

        return driverService.getDriver(driverId);
    }


    // Approve a driver
    @PatchMapping("/{driverId}/approve")
    public DriverResponse approveDriver(
            @PathVariable UUID driverId,
            @AuthenticationPrincipal Jwt jwt) {

        return driverService.approveDriver(
                driverId,
                jwt.getSubject()
        );
    }


    // Reject a driver
    @PatchMapping("/{driverId}/reject")
    public DriverResponse rejectDriver(
            @PathVariable UUID driverId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RejectDriverRequest request) {

        return driverService.rejectDriver(
                driverId,
                jwt.getSubject(),
                request.reason()
        );
    }
}