package com.dual.dev.account_service.account.service;

import com.dual.dev.account_service.account.dto.UpdateAccountRequest;
import com.dual.dev.account_service.account.dto.AccountResponse;
import com.dual.dev.account_service.account.entity.Account;
import com.dual.dev.account_service.account.repository.AccountRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Create an account for a Keycloak user if one doesn't exist
    public AccountResponse provisionMyAccount(
            String keycloakUserId,
            String email) {

        // 1. Check whether an account already exists
        Account existingAccount = accountRepository
                .findByKeycloakUserId(keycloakUserId)
                .orElse(null);

        // 2. If it exists, return the existing account
        if (existingAccount != null) {
            return toResponse(existingAccount);
        }

        // 3. Check whether email is available
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email claim is missing from the JWT token"
            );
        }

        if (accountRepository.existsByEmail(email)) {
            throw new IllegalStateException(
                    "An account with this email already exists"
            );
        }

        // 4. Create a new account
        Account account = new Account();

        account.setKeycloakUserId(keycloakUserId);
        account.setEmail(email);

        // First name, last name and phone number
        // will be provided later by the user.

        // 5. Save the account in PostgreSQL
        Account savedAccount = accountRepository.save(account);

        // 6. Return the account
        return toResponse(savedAccount);
    }

    // Get the account belonging to a Keycloak user
    @Transactional(readOnly = true)
    public AccountResponse getMyAccount(String keycloakUserId) {

        Account account = accountRepository
                .findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        return toResponse(account);
    }

    // Update the account belonging to a Keycloak user
    public AccountResponse updateMyAccount(
            String keycloakUserId,
            UpdateAccountRequest request) {

        Account account = accountRepository
                .findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        account.setFirstName(request.firstName());
        account.setLastName(request.lastName());
        account.setPhoneNumber(request.phoneNumber());

        Account updatedAccount = accountRepository.save(account);

        return toResponse(updatedAccount);
    }

    // Convert Account entity into AccountResponse DTO
    private AccountResponse toResponse(Account account) {

        return new AccountResponse(
                account.getId(),
                account.getEmail(),
                account.getFirstName(),
                account.getLastName(),
                account.getPhoneNumber(),
                account.isActive(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}