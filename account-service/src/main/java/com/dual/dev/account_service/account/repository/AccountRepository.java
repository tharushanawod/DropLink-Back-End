
package com.dual.dev.account_service.account.repository;

import com.dual.dev.account_service.account.entity.Account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByKeycloakUserId(String keycloakUserId);

    Optional<Account> findByEmail(String email);

    boolean existsByKeycloakUserId(String keycloakUserId);

    boolean existsByEmail(String email);
}