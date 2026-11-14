package com.dual.dev.account_service.driver.profile.service;

import com.dual.dev.account_service.account.entity.Account;
import com.dual.dev.account_service.account.repository.AccountRepository;

import com.dual.dev.account_service.common.exception.BadRequestException;
import com.dual.dev.account_service.common.exception.ConflictException;
import com.dual.dev.account_service.common.exception.ResourceNotFoundException;

import com.dual.dev.account_service.driver.profile.dto.request.DriverProfileRequest;
import com.dual.dev.account_service.driver.profile.dto.response.DriverLicenseDocumentResponse;
import com.dual.dev.account_service.driver.profile.dto.response.DriverResponse;

import com.dual.dev.account_service.driver.profile.entity.Driver;
import com.dual.dev.account_service.driver.profile.enums.DriverVerificationStatus;
import com.dual.dev.account_service.driver.profile.repository.DriverRepository;

import com.dual.dev.account_service.storage.DriverLicenseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final AccountRepository accountRepository;
    private final DriverLicenseStorageService driverLicenseStorageService;



    // =========================================================
    // DRIVER OPERATIONS
    // =========================================================

    // Create a driver profile for the authenticated user
    public DriverResponse createMyDriverProfile(
            String keycloakUserId,
            DriverProfileRequest request) {

        Account account = getActiveAccount(keycloakUserId);

        // An Account can have only one Driver profile
        if (driverRepository.existsByAccount_Id(account.getId())) {

            throw new ConflictException(
                    "A driver profile already exists for this account"
            );
        }

        String licenseNumber = normalizeLicenseNumber(
                request.licenseNumber()
        );

        String issuingCountry = normalizeCountry(
                request.licenseIssuingCountry()
        );

        // Check licence uniqueness
        ensureLicenseAvailable(licenseNumber, issuingCountry, null);

        Driver driver = new Driver();

        driver.setAccount(account);

        applyProfileInformation(
                driver,
                request,
                licenseNumber,
                issuingCountry
        );

        driver.setVerificationStatus(
                DriverVerificationStatus.DRAFT
        );

        Driver savedDriver = driverRepository.save(driver);

        return toResponse(savedDriver);
    }


    // Retrieve the authenticated user's driver profile
    @Transactional(readOnly = true)
    public DriverResponse getMyDriverProfile(String keycloakUserId) {

        Account account = getActiveAccount(keycloakUserId);

        Driver driver = driverRepository
                .findByAccount_Id(account.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver profile not found"
                        )
                );

        return toResponse(driver);
    }


    // Update the authenticated user's driver profile
    public DriverResponse updateMyDriverProfile(
            String keycloakUserId,
            DriverProfileRequest request) {

        Account account = getActiveAccount(keycloakUserId);

        Driver driver = driverRepository
                .findByAccount_Id(account.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver profile not found"
                        )
                );

        // Profiles can only be edited while DRAFT or REJECTED
        validateEditableStatus(driver);

        String licenseNumber = normalizeLicenseNumber(
                request.licenseNumber()
        );

        String issuingCountry = normalizeCountry(
                request.licenseIssuingCountry()
        );

        ensureLicenseAvailable(
                licenseNumber,
                issuingCountry,
                driver.getId()
        );

        applyProfileInformation(
                driver,
                request,
                licenseNumber,
                issuingCountry
        );

        Driver updatedDriver = driverRepository.save(driver);

        return toResponse(updatedDriver);
    }


    // Submit a driver profile for administrator verification
    @Transactional
    public DriverResponse submitForVerification(String keycloakUserId) {

        // 1. Find the driver's active account.
        Account account = getActiveAccount(keycloakUserId);

        // 2. Find the driver's profile.
        Driver driver = driverRepository.findByAccount_Id(account.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver profile not found.")
                );

        // 3. Check the current verification status.
        if (driver.getVerificationStatus() != DriverVerificationStatus.DRAFT
                && driver.getVerificationStatus() != DriverVerificationStatus.REJECTED) {

            throw new ConflictException(
                    "Your driver profile cannot be submitted in its current status."
            );
        }

        // 4. Make sure the required licence information exists.
        if (driver.getLicenseNumber() == null
                || driver.getLicenseNumber().isBlank()
                || driver.getLicenseClass() == null
                || driver.getLicenseClass().isBlank()
                || driver.getLicenseIssuingCountry() == null
                || driver.getLicenseIssuingCountry().isBlank()) {

            throw new BadRequestException(
                    "Please complete your licence information before submitting."
            );
        }

        // 5. Make sure the licence document has been uploaded.
        if (driver.getLicenseDocumentKey() == null
                || driver.getLicenseDocumentKey().isBlank()) {

            throw new BadRequestException(
                    "Please upload your licence document before submitting."
            );
        }

        // 6. Check whether the licence is expired.
        if (driver.getLicenseExpiryDate() == null
                || driver.getLicenseExpiryDate().isBefore(LocalDate.now())) {

            throw new BadRequestException(
                    "Your driving licence has expired or has no valid expiry date."
            );
        }

        // 7. Change the verification status.
        driver.setVerificationStatus(DriverVerificationStatus.PENDING);

        // 8. Record the submission time.
        driver.setSubmittedAt(Instant.now());

        // 9. Clear previous review information when resubmitting.
        driver.setReviewedAt(null);
        driver.setReviewedBy(null);
        driver.setRejectionReason(null);

        Driver savedDriver = driverRepository.save(driver);

        return toResponse(savedDriver);
    }


    // =========================================================
    // ADMINISTRATOR OPERATIONS
    // =========================================================

    // Retrieve drivers, optionally filtering by verification status
    @Transactional(readOnly = true)
    public Page<DriverResponse> getDrivers(
            String status,
            Pageable pageable) {

        DriverVerificationStatus verificationStatus =
                parseVerificationStatus(status);

        Page<Driver> drivers;

        if (verificationStatus == null) {

            drivers = driverRepository.findAll(pageable);

        } else {

            drivers = driverRepository.findAllByVerificationStatus(
                    verificationStatus,
                    pageable
            );
        }

        return drivers.map(this::toResponse);
    }


    // Retrieve a particular driver's profile
    @Transactional(readOnly = true)
    public DriverResponse getDriver(UUID driverId) {

        Driver driver = getDriverEntity(driverId);

        return toResponse(driver);
    }


    // Approve a pending driver profile
    public DriverResponse approveDriver(
            UUID driverId,
            String adminKeycloakUserId) {

        Driver driver = getDriverEntity(driverId);

        Account administrator = getActiveAccount(
                adminKeycloakUserId
        );

        validateReviewer(driver, administrator);

        validatePendingStatus(driver);

        // Licence may have expired while awaiting review
        validateLicenseNotExpired(driver);

        driver.setVerificationStatus(
                DriverVerificationStatus.APPROVED
        );

        driver.setReviewedAt(Instant.now());
        driver.setReviewedBy(administrator);
        driver.setRejectionReason(null);

        Driver approvedDriver = driverRepository.save(driver);

        return toResponse(approvedDriver);
    }


    // Reject a pending driver profile
    public DriverResponse rejectDriver(
            UUID driverId,
            String adminKeycloakUserId,
            String reason) {

        Driver driver = getDriverEntity(driverId);

        Account administrator = getActiveAccount(
                adminKeycloakUserId
        );

        validateReviewer(driver, administrator);

        validatePendingStatus(driver);

        driver.setVerificationStatus(
                DriverVerificationStatus.REJECTED
        );

        driver.setRejectionReason(reason.trim());

        driver.setReviewedAt(Instant.now());
        driver.setReviewedBy(administrator);

        Driver rejectedDriver = driverRepository.save(driver);

        return toResponse(rejectedDriver);
    }


    // =========================================================
    // PRIVATE HELPER METHODS
    // =========================================================

    // Retrieve an active Account using its Keycloak user ID
    private Account getActiveAccount(String keycloakUserId) {

        Account account = accountRepository
                .findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found"
                        )
                );

        if (!account.isActive()) {

            throw new AccessDeniedException(
                    "Inactive accounts cannot perform this operation"
            );
        }

        return account;
    }


    // Retrieve a Driver entity using its database ID
    private Driver getDriverEntity(UUID driverId) {

        return driverRepository
                .findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver profile not found"
                        )
                );
    }


    // Apply the editable profile information
    private void applyProfileInformation(
            Driver driver,
            DriverProfileRequest request,
            String licenseNumber,
            String issuingCountry) {

        driver.setLicenseNumber(licenseNumber);

        driver.setLicenseClass(
                request.licenseClass().trim().toUpperCase(Locale.ROOT)
        );

        driver.setLicenseIssuingCountry(issuingCountry);

        driver.setLicenseExpiryDate(
                request.licenseExpiryDate()
        );
    }


    // Ensure that the profile can currently be edited
    private void validateEditableStatus(Driver driver) {

        DriverVerificationStatus status =
                driver.getVerificationStatus();

        if (status != DriverVerificationStatus.DRAFT
                && status != DriverVerificationStatus.REJECTED) {

            throw new ConflictException(
                    "Driver profile cannot be edited in its current verification status"
            );
        }
    }


    // Ensure that the licence is unique for its issuing country
    private void ensureLicenseAvailable(
            String licenseNumber,
            String issuingCountry,
            UUID currentDriverId) {

        boolean exists;

        if (currentDriverId == null) {

            exists = driverRepository
                    .existsByLicenseNumberAndLicenseIssuingCountry(
                            licenseNumber,
                            issuingCountry
                    );

        } else {

            exists = driverRepository
                    .existsByLicenseNumberAndLicenseIssuingCountryAndIdNot(
                            licenseNumber,
                            issuingCountry,
                            currentDriverId
                    );
        }

        if (exists) {

            throw new ConflictException(
                    "This driving licence is already registered"
            );
        }
    }


    // Normalize licence number
    private String normalizeLicenseNumber(String licenseNumber) {

        return licenseNumber
                .trim()
                .toUpperCase(Locale.ROOT);
    }


    // Normalize issuing country
    private String normalizeCountry(String country) {

        return country
                .trim()
                .toUpperCase(Locale.ROOT);
    }


    // Prevent submission or approval of expired licences
    private void validateLicenseNotExpired(Driver driver) {

        if (driver.getLicenseExpiryDate().isBefore(LocalDate.now())) {

            throw new BadRequestException(
                    "The driving licence has expired"
            );
        }
    }


    // Ensure the profile is waiting for administrator review
    private void validatePendingStatus(Driver driver) {

        if (driver.getVerificationStatus()
                != DriverVerificationStatus.PENDING) {

            throw new ConflictException(
                    "Only PENDING driver profiles can be reviewed"
            );
        }
    }


    // Prevent an administrator from approving or rejecting their own profile
    private void validateReviewer(
            Driver driver,
            Account administrator) {

        if (driver.getAccount().getId().equals(administrator.getId())) {

            throw new AccessDeniedException(
                    "Administrators cannot review their own driver profiles"
            );
        }
    }


    // Parse and validate an optional verification-status filter
    private DriverVerificationStatus parseVerificationStatus(
            String status) {

        if (status == null || status.isBlank()) {
            return null;
        }

        try {

            return DriverVerificationStatus.valueOf(
                    status.trim().toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException exception) {

            throw new BadRequestException(
                    "Invalid verification status. Allowed values: DRAFT, PENDING, APPROVED, REJECTED"
            );
        }
    }


    @Transactional
    public DriverLicenseDocumentResponse uploadMyLicenseDocument(
            String keycloakUserId,
            MultipartFile file
    ) {

        // 1. Find the currently authenticated driver's active account.
        Account account = getActiveAccount(keycloakUserId);

        // 2. Find the driver's profile.
        Driver driver = driverRepository.findByAccount_Id(account.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver profile not found.")
                );

        // 3. Only DRAFT and REJECTED profiles can upload or replace documents.
        if (driver.getVerificationStatus() != DriverVerificationStatus.DRAFT
                && driver.getVerificationStatus() != DriverVerificationStatus.REJECTED) {

            throw new ConflictException(
                    "You cannot upload a licence document in the current verification status."
            );
        }

        // 4. Upload the new document to RustFS.
        DriverLicenseStorageService.UploadedLicense uploaded =
                driverLicenseStorageService.upload(driver.getId(), file);

        // 5. Remember the old document key, if a document already exists.
        String previousDocumentKey = driver.getLicenseDocumentKey();

        Instant uploadedAt = Instant.now();

        // 6. Update the document information in PostgreSQL.
        driver.setLicenseDocumentKey(uploaded.objectKey());
        driver.setLicenseDocumentFileName(uploaded.fileName());
        driver.setLicenseDocumentContentType(uploaded.contentType());
        driver.setLicenseDocumentSize(uploaded.size());
        driver.setLicenseDocumentUploadedAt(uploadedAt);

        try {

            driverRepository.save(driver);

        } catch (RuntimeException exception) {

            // If saving PostgreSQL data fails, remove the newly uploaded file.
            driverLicenseStorageService.deleteQuietly(uploaded.objectKey());

            throw exception;
        }

        // 7. Coordinate file cleanup with the database transaction.
        // Delete the old file only after the database transaction commits.
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {

                        if (previousDocumentKey != null
                                && !previousDocumentKey.isBlank()) {

                            driverLicenseStorageService.deleteQuietly(
                                    previousDocumentKey
                            );
                        }
                    }

                    @Override
                    public void afterCompletion(int status) {

                        // If the database transaction rolls back,
                        // remove the newly uploaded object.
                        if (status != STATUS_COMMITTED) {

                            driverLicenseStorageService.deleteQuietly(
                                    uploaded.objectKey()
                            );
                        }
                    }
                }
        );

        // 8. Return the upload result.
        return new DriverLicenseDocumentResponse(
                uploaded.fileName(),
                uploaded.contentType(),
                uploaded.size(),
                uploadedAt,
                "Licence document uploaded successfully."
        );
    }



    // Convert Driver entity into response DTO
    private DriverResponse toResponse(Driver driver) {

        Account account = driver.getAccount();

        Account reviewer = driver.getReviewedBy();

        return new DriverResponse(

                driver.getId(),

                account.getId(),

                account.getEmail(),

                account.getFirstName(),

                account.getLastName(),

                account.getPhoneNumber(),

                driver.getLicenseNumber(),

                driver.getLicenseClass(),

                driver.getLicenseIssuingCountry(),

                driver.getLicenseExpiryDate(),

                driver.getVerificationStatus(),

                driver.getRejectionReason(),

                driver.getSubmittedAt(),

                driver.getReviewedAt(),

                reviewer != null ? reviewer.getId() : null,

                driver.getCreatedAt(),

                driver.getUpdatedAt()
        );
    }
}