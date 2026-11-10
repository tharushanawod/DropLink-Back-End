package com.dual.dev.account_service.security;

import com.dual.dev.account_service.account.entity.Account;
import com.dual.dev.account_service.account.repository.AccountRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("accountStatusChecker")
public class AccountStatusChecker {

    private final AccountRepository accountRepository;

    public AccountStatusChecker(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Check whether the authenticated user's account is active
    public boolean isActive(Authentication authentication) {

        String keycloakUserId = getKeycloakUserId(authentication);

        if (keycloakUserId == null) {
            return false;
        }

        return accountRepository
                .findByKeycloakUserId(keycloakUserId)
                .map(Account::isActive)
                .orElse(false);
    }

    // Allow users who have not created an account yet,
    // but reject existing inactive accounts.
    public boolean isActiveOrNotProvisioned(
            Authentication authentication) {

        String keycloakUserId = getKeycloakUserId(authentication);

        if (keycloakUserId == null) {
            return false;
        }

        return accountRepository
                .findByKeycloakUserId(keycloakUserId)
                .map(Account::isActive)
                .orElse(true);
    }

    // Extract the Keycloak user ID from the authenticated JWT
    private String getKeycloakUserId(Authentication authentication) {

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }

        String keycloakUserId = jwt.getSubject();

        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            return null;
        }

        return keycloakUserId;
    }
}