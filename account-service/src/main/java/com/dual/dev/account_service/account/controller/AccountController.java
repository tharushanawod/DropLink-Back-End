package com.dual.dev.account_service.account.controller;

import com.dual.dev.account_service.account.dto.request.UpdateAccountRequest;
import com.dual.dev.account_service.account.dto.response.AccountResponse;
import com.dual.dev.account_service.account.service.AccountService;

import com.dual.dev.account_service.common.dto.PageResponse;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // Create an account for the logged-in user
    @PostMapping("/provision")
    public AccountResponse provisionMyAccount(
            @AuthenticationPrincipal Jwt jwt) {

        String keycloakUserId = jwt.getSubject();

        String email = jwt.getClaimAsString("email");

        return accountService.provisionMyAccount(
                keycloakUserId,
                email
        );
    }

    // Fetch all accounts - admin only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<AccountResponse> getAllAccounts(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        Page<AccountResponse> accountPage =
                accountService.getAllAccounts(pageable);

        return new PageResponse<>(
                accountPage.getContent(),
                accountPage.getNumber(),
                accountPage.getSize(),
                accountPage.getTotalElements(),
                accountPage.getTotalPages(),
                accountPage.isFirst(),
                accountPage.isLast()
        );
    }
}