package com.dual.dev.account_service.account.controller;

import com.dual.dev.account_service.account.dto.UpdateAccountRequest;
import com.dual.dev.account_service.account.dto.AccountResponse;
import com.dual.dev.account_service.account.service.AccountService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Get the logged-in user's account
    @GetMapping("/me")
    public AccountResponse getMyAccount(
            @AuthenticationPrincipal Jwt jwt) {

        String keycloakUserId = jwt.getSubject();

        return accountService.getMyAccount(keycloakUserId);
    }

    // Update the logged-in user's account
    @PutMapping("/me")
    public AccountResponse updateMyAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateAccountRequest request) {

        String keycloakUserId = jwt.getSubject();

        return accountService.updateMyAccount(
                keycloakUserId,
                request
        );
    }
}