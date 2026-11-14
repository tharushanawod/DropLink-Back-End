
package com.dual.dev.account_service.driver.profile.controller;

import com.dual.dev.account_service.driver.profile.dto.request.DriverProfileRequest;
import com.dual.dev.account_service.driver.profile.dto.response.DriverLicenseDocumentResponse;
import com.dual.dev.account_service.driver.profile.dto.response.DriverResponse;
import com.dual.dev.account_service.driver.profile.service.DriverService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/drivers/me")
@PreAuthorize("hasRole('CUSTOMER') and @accountStatusChecker.isActive(authentication)")
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


    // Upload driving licence document
    @PostMapping(
            value = "/license-document",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DriverLicenseDocumentResponse> uploadLicenseDocument(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file
    ) {

        DriverLicenseDocumentResponse response =
                driverService.uploadMyLicenseDocument(
                        jwt.getSubject(),
                        file
                );

        return ResponseEntity.ok(response);
    }


    // Submit driver profile for administrator verification
    @PostMapping("/submit")
    public ResponseEntity<DriverResponse> submitForVerification(
            @AuthenticationPrincipal Jwt jwt
    ) {

        DriverResponse response =
                driverService.submitForVerification(jwt.getSubject());

        return ResponseEntity.ok(response);
    }
}
