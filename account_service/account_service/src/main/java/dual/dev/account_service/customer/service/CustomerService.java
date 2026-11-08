
package com.dual.dev.account_service.customer.service;

import com.dual.dev.account_service.customer.dto.*;
import com.dual.dev.account_service.customer.entity.Customer;
import com.dual.dev.account_service.customer.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    // 1. Create customer profile
    @Transactional
    public CustomerResponse createMyProfile(
            String keycloakUserId,
            CreateCustomerRequest request
    ) {

        if (customerRepository.existsByKeycloakUserId(keycloakUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer profile already exists"
            );
        }

        Customer customer = new Customer();

        customer.setKeycloakUserId(keycloakUserId);
        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        customer.setPhone(request.phone());

        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        return toResponse(savedCustomer);
    }

    // 2. Get current customer profile
    public CustomerResponse getMyProfile(String keycloakUserId) {

        Customer customer = customerRepository
                .findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer profile not found"
                ));

        return toResponse(customer);
    }

    // 3. Update current customer profile
    @Transactional
    public CustomerResponse updateMyProfile(
            String keycloakUserId,
            UpdateCustomerRequest request
    ) {

        Customer customer = customerRepository
                .findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer profile not found"
                ));

        if (request.firstName() != null) {
            customer.setFirstName(request.firstName().trim());
        }

        if (request.lastName() != null) {
            customer.setLastName(request.lastName().trim());
        }

        if (request.phone() != null) {
            customer.setPhone(request.phone());
        }

        // Flush changes before generating the response.
        customerRepository.flush();

        return toResponse(customer);
    }

    // 4. Retrieve customer by ID
    public CustomerResponse getCustomerById(UUID customerId) {

        Customer customer = customerRepository
                .findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found"
                ));

        return toResponse(customer);
    }

    // Convert Customer entity to CustomerResponse DTO
    private CustomerResponse toResponse(Customer customer) {

        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhone(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
