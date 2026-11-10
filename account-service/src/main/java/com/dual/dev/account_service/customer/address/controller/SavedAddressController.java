package com.dual.dev.account_service.customer.address.controller;

import com.dual.dev.account_service.customer.address.dto.request.CreateSavedAddressRequest;
import com.dual.dev.account_service.customer.address.dto.request.UpdateSavedAddressRequest;
import com.dual.dev.account_service.customer.address.dto.response.SavedAddressResponse;

import com.dual.dev.account_service.customer.address.service.SavedAddressService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/me/addresses")
@PreAuthorize("hasRole('CUSTOMER') and @accountStatusChecker.isActive(authentication)")
public class SavedAddressController {

    private final SavedAddressService savedAddressService;

    public SavedAddressController(
            SavedAddressService savedAddressService) {

        this.savedAddressService = savedAddressService;
    }


    // Create a saved address
    @PostMapping
    public ResponseEntity<SavedAddressResponse> createAddress(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateSavedAddressRequest request) {

        SavedAddressResponse response =
                savedAddressService.createAddress(
                        jwt.getSubject(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // Retrieve all saved addresses
    @GetMapping
    public List<SavedAddressResponse> getMyAddresses(
            @AuthenticationPrincipal Jwt jwt) {

        return savedAddressService.getMyAddresses(
                jwt.getSubject()
        );
    }


    // Retrieve one saved address
    @GetMapping("/{addressId}")
    public SavedAddressResponse getMyAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID addressId) {

        return savedAddressService.getMyAddress(
                jwt.getSubject(),
                addressId
        );
    }


    // Update a saved address
    @PutMapping("/{addressId}")
    public SavedAddressResponse updateAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateSavedAddressRequest request) {

        return savedAddressService.updateAddress(
                jwt.getSubject(),
                addressId,
                request
        );
    }


    // Delete a saved address
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID addressId) {

        savedAddressService.deleteAddress(
                jwt.getSubject(),
                addressId
        );

        return ResponseEntity.noContent().build();
    }


    // Make an address the default address
    @PatchMapping("/{addressId}/default")
    public SavedAddressResponse setDefaultAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID addressId) {

        return savedAddressService.setDefaultAddress(
                jwt.getSubject(),
                addressId
        );
    }
}