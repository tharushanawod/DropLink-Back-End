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