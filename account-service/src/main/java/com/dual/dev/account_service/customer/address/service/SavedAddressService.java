package com.dual.dev.account_service.customer.address.service;

import com.dual.dev.account_service.account.entity.Account;
import com.dual.dev.account_service.account.repository.AccountRepository;

import com.dual.dev.account_service.common.exception.BadRequestException;
import com.dual.dev.account_service.common.exception.ResourceNotFoundException;

import com.dual.dev.account_service.customer.address.dto.request.CreateSavedAddressRequest;
import com.dual.dev.account_service.customer.address.dto.request.UpdateSavedAddressRequest;
import com.dual.dev.account_service.customer.address.dto.response.SavedAddressResponse;

import com.dual.dev.account_service.customer.address.entity.SavedAddress;
import com.dual.dev.account_service.customer.address.repository.SavedAddressRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SavedAddressService {

    private final SavedAddressRepository savedAddressRepository;
    private final AccountRepository accountRepository;

    public SavedAddressService(
            SavedAddressRepository savedAddressRepository,
            AccountRepository accountRepository) {

        this.savedAddressRepository = savedAddressRepository;
        this.accountRepository = accountRepository;
    }


    // Create a saved address for the authenticated customer
    public SavedAddressResponse createAddress(
            String keycloakUserId,
            CreateSavedAddressRequest request) {

        Account account = getAccount(keycloakUserId);

        validateCoordinates(
                request.latitude(),
                request.longitude()
        );

        // Create a SavedAddress object
        SavedAddress address = new SavedAddress();

        address.setAccount(account);

        address.setLabel(request.label());
        address.setRecipientName(request.recipientName());
        address.setRecipientPhone(request.recipientPhone());

        address.setAddressLine1(request.addressLine1());
        address.setAddressLine2(request.addressLine2());

        address.setCity(request.city());
        address.setDistrict(request.district());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country());

        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());

        // Automatically make the first saved address the default
        boolean firstAddress =
                !savedAddressRepository.existsByAccount_Id(account.getId());

        address.setDefaultAddress(firstAddress);

        SavedAddress savedAddress =
                savedAddressRepository.save(address);

        return toResponse(savedAddress);
    }


    // Retrieve all addresses belonging to the authenticated customer
    @Transactional(readOnly = true)
    public List<SavedAddressResponse> getMyAddresses(
            String keycloakUserId) {

        Account account = getAccount(keycloakUserId);

        return savedAddressRepository
                .findAllByAccount_IdOrderByDefaultAddressDescCreatedAtDesc(
                        account.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // Retrieve one address belonging to the authenticated customer
    @Transactional(readOnly = true)
    public SavedAddressResponse getMyAddress(
            String keycloakUserId,
            UUID addressId) {

        Account account = getAccount(keycloakUserId);

        SavedAddress address = getOwnedAddress(
                addressId,
                account.getId()
        );

        return toResponse(address);
    }


    // Update an existing saved address
    public SavedAddressResponse updateAddress(
            String keycloakUserId,
            UUID addressId,
            UpdateSavedAddressRequest request) {

        Account account = getAccount(keycloakUserId);

        SavedAddress address = getOwnedAddress(
                addressId,
                account.getId()
        );

        validateCoordinates(
                request.latitude(),
                request.longitude()
        );

        address.setLabel(request.label());
        address.setRecipientName(request.recipientName());
        address.setRecipientPhone(request.recipientPhone());

        address.setAddressLine1(request.addressLine1());
        address.setAddressLine2(request.addressLine2());

        address.setCity(request.city());
        address.setDistrict(request.district());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country());

        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());

        SavedAddress updatedAddress =
                savedAddressRepository.save(address);

        return toResponse(updatedAddress);
    }


    // Delete a saved address
    public void deleteAddress(
            String keycloakUserId,
            UUID addressId) {

        Account account = getAccount(keycloakUserId);

        SavedAddress address = getOwnedAddress(
                addressId,
                account.getId()
        );

        boolean wasDefault = address.isDefaultAddress();

        savedAddressRepository.delete(address);

        // If the default address was deleted,
        // make another remaining address the default.
        if (wasDefault) {

            savedAddressRepository.flush();

            savedAddressRepository
                    .findFirstByAccount_IdOrderByCreatedAtAsc(
                            account.getId()
                    )
                    .ifPresent(nextAddress -> {

                        nextAddress.setDefaultAddress(true);

                        savedAddressRepository.save(nextAddress);
                    });
        }
    }


    // Make an address the customer's default address
    public SavedAddressResponse setDefaultAddress(
            String keycloakUserId,
            UUID addressId) {

        Account account = getAccount(keycloakUserId);

        SavedAddress address = getOwnedAddress(
                addressId,
                account.getId()
        );

        if (address.isDefaultAddress()) {
            return toResponse(address);
        }

        // Remove the default status from previous addresses
        clearExistingDefaultAddress(account.getId());

        // Set the selected address as the new default
        address.setDefaultAddress(true);

        SavedAddress updatedAddress =
                savedAddressRepository.save(address);

        return toResponse(updatedAddress);
    }


    // Find the Account belonging to the authenticated Keycloak user
    private Account getAccount(String keycloakUserId) {

        return accountRepository
                .findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found"
                        )
                );
    }


    // Find an address only when it belongs to the specified account
    private SavedAddress getOwnedAddress(
            UUID addressId,
            UUID accountId) {

        return savedAddressRepository
                .findByIdAndAccount_Id(addressId, accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Saved address not found"
                        )
                );
    }


    // Remove the default status from existing addresses
    private void clearExistingDefaultAddress(UUID accountId) {

        List<SavedAddress> defaultAddresses =
                savedAddressRepository
                        .findAllByAccount_IdAndDefaultAddressTrue(
                                accountId
                        );

        for (SavedAddress address : defaultAddresses) {

            address.setDefaultAddress(false);
        }

        savedAddressRepository.saveAll(defaultAddresses);
    }


    // Coordinates must either both be provided or both be absent
    private void validateCoordinates(
            java.math.BigDecimal latitude,
            java.math.BigDecimal longitude) {

        if ((latitude == null) != (longitude == null)) {

            throw new BadRequestException(
                    "Latitude and longitude must be provided together"
            );
        }
    }


    // Convert SavedAddress entity into response DTO
    private SavedAddressResponse toResponse(SavedAddress address) {

        return new SavedAddressResponse(
                address.getId(),
                address.getLabel(),
                address.getRecipientName(),
                address.getRecipientPhone(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getDistrict(),
                address.getPostalCode(),
                address.getCountry(),
                address.getLatitude(),
                address.getLongitude(),
                address.isDefaultAddress(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
}