
package com.dual.dev.account_service.customer.controller;

import com.dual.dev.account_service.customer.dto.CreateCustomerRequest;
import com.dual.dev.account_service.customer.dto.UpdateCustomerRequest;
import com.dual.dev.account_service.customer.dto.CustomerResponse;
import com.dual.dev.account_service.customer.service.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // 1. Create logged-in customer's profile
    @PostMapping("/me")
    public ResponseEntity<CustomerResponse> createMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        CustomerResponse response = customerService.createMyProfile(
                jwt.getSubject(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 2. Get logged-in customer's profile
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {
        CustomerResponse response = customerService.getMyProfile(
                jwt.getSubject()
        );

        return ResponseEntity.ok(response);
    }

    // 3. Update logged-in customer's profile
    @PatchMapping("/me")
    public ResponseEntity<CustomerResponse> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        CustomerResponse response = customerService.updateMyProfile(
                jwt.getSubject(),
                request
        );

        return ResponseEntity.ok(response);
    }
}
