package com.dual.dev.account_service.customer.repository;

import com.dual.dev.account_service.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository
        extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByKeycloakUserId(
            String keycloakUserId
    );

    boolean existsByKeycloakUserId(
            String keycloakUserId
    );
}