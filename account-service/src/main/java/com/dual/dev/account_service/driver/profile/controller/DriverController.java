package com.dual.dev.account_service.driver.profile.controller;

import com.dual.dev.account_service.driver.profile.dto.request.DriverProfileRequest;
import com.dual.dev.account_service.driver.profile.dto.response.DriverResponse;
import com.dual.dev.account_service.driver.profile.service.DriverService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/drivers/me")
@PreAuthorize("hasRole('DRIVER') and @accountStatusChecker.isActive(authentication)")
public class DriverController {

    private final DriverService driverService;

    // Create driver profile
    @PostMapping
    public ResponseEntity<DriverResponse> createMyDriverProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DriverProfileRequest request) {

        DriverResponse response =
                driverService.createMyDriverProfile(
                        jwt.getSubject(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // Retrieve own driver profile
    @GetMapping
    public DriverResponse getMyDriverProfile(
            @AuthenticationPrincipal Jwt jwt) {

        return driverService.getMyDriverProfile(
                jwt.getSubject()
        );
    }


    // Update own driver profile
    @PutMapping
    public DriverResponse updateMyDriverProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DriverProfileRequest request) {

        return driverService.updateMyDriverProfile(
                jwt.getSubject(),
                request
        );
    }


    // Submit profile for administrator verification
    @PostMapping("/submit")
    public DriverResponse submitForVerification(
            @AuthenticationPrincipal Jwt jwt) {

        return driverService.submitForVerification(
                jwt.getSubject()
        );
    }
}