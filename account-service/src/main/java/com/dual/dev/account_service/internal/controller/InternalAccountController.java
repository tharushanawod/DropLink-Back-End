package com.dual.dev.account_service.internal.controller;

import com.dual.dev.account_service.account.entity.Account;
import com.dual.dev.account_service.account.repository.AccountRepository;
import com.dual.dev.account_service.internal.dto.AccountStatusResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalAccountController {

    private final AccountRepository accountRepository;

    @GetMapping("/me/account-status")
    public AccountStatusResponse getCurrentAccountStatus(
            @AuthenticationPrincipal Jwt jwt
    ) {

        boolean active = accountRepository
                .findByKeycloakUserId(jwt.getSubject())
                .map(Account::isActive)
                .orElse(false);

        return new AccountStatusResponse(active);
    }
}