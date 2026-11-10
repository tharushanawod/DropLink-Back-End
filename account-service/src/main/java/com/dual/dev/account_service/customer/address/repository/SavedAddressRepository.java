package com.dual.dev.account_service.customer.address.repository;

import com.dual.dev.account_service.customer.address.entity.SavedAddress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedAddressRepository
        extends JpaRepository<SavedAddress, UUID> {

    // Retrieve all addresses belonging to a particular account
    List<SavedAddress> findAllByAccount_IdOrderByDefaultAddressDescCreatedAtDesc(
            UUID accountId
    );

    // Find an address only if it belongs to the specified account
    Optional<SavedAddress> findByIdAndAccount_Id(
            UUID addressId,
            UUID accountId
    );

    // Check whether an account already has saved addresses
    boolean existsByAccount_Id(UUID accountId);

    // Find the current default address or addresses
    List<SavedAddress> findAllByAccount_IdAndDefaultAddressTrue(
            UUID accountId
    );

    // Find the oldest remaining address
    Optional<SavedAddress> findFirstByAccount_IdOrderByCreatedAtAsc(
            UUID accountId
    );
}