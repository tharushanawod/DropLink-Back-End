
package com.dual.dev.account_service.driver.profile.service;

import com.dual.dev.account_service.account.entity.Account;
import com.dual.dev.account_service.account.repository.AccountRepository;

import com.dual.dev.account_service.common.exception.BadRequestException;
import com.dual.dev.account_service.common.exception.ConflictException;
import com.dual.dev.account_service.common.exception.ResourceNotFoundException;

import com.dual.dev.account_service.driver.profile.dto.request.DriverProfileRequest;
import com.dual.dev.account_service.driver.profile.dto.response.DriverLicenseDocumentResponse;
import com.dual.dev.account_service.driver.profile.dto.response.DriverResponse;
import com.dual.dev.account_service.driver.profile.dto.response.DriverVerificationResponse;

import com.dual.dev.account_service.driver.profile.entity.Driver;
import com.dual.dev.account_service.driver.profile.entity.DriverVerification;

import com.dual.dev.account_service.driver.profile.enums.DriverVerificationStatus;

import com.dual.dev.account_service.driver.profile.repository.DriverRepository;
import com.dual.dev.account_service.driver.profile.repository.DriverVerificationRepository;

import com.dual.dev.account_service.storage.DriverLicenseStorageService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final DriverVerificationRepository driverVerificationRepository;


    // =========================================================
    // DRIVER APPLICATION OPERATIONS
    // =========================================================


    // Create a driver profile for the authenticated customer
    public DriverResponse createMyDriverProfile(
            String keycloakUserId,
            DriverProfileRequest request) {

        Account account = getActiveAccount(keycloakUserId);

        // An Account can have only one Driver profile.
        if (driverRepository.existsByAccount_Id(account.getId())) {

            throw new ConflictException(
                    "A driver profile already exists for this account."
            );
        }

        String licenseNumber = normalizeLicenseNumber(
                request.licenseNumber()
        );

        String issuingCountry = normalizeCountry(
                request.licenseIssuingCountry()
        );

        // Check whether the licence is already registered.
        ensureLicenseAvailable(
                licenseNumber,
                issuingCountry,
                null
        );

        // Create the Driver profile.
        Driver driver = new Driver();

        driver.setAccount(account);

        applyProfileInformation(
                driver,
                request,
                licenseNumber,
                issuingCountry
        );

        Driver savedDriver = driverRepository.save(driver);

        // Every new Driver starts with an initial DRAFT verification.
        DriverVerification verification = new DriverVerification();

        verification.setDriver(savedDriver);
        verification.setStatus(DriverVerificationStatus.DRAFT);

        DriverVerification savedVerification =
                driverVerificationRepository.save(verification);

        return toResponse(savedDriver, savedVerification);
    }


    // Retrieve the authenticated user's driver profile
    @Transactional(readOnly = true)
    public DriverResponse getMyDriverProfile(String keycloakUserId) {

        Account account = getActiveAccount(keycloakUserId);

        Driver driver = driverRepository
                .findByAccount_Id(account.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver profile not found."
                        )
                );

        DriverVerification verification =
                getLatestVerification(driver);

        return toResponse(driver, verification);
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
                                "Driver profile not found."
                        )
                );

        // Get the current DRAFT verification.
        // If the latest application was REJECTED, this creates
        // a new DRAFT verification without modifying the old one.
        DriverVerification verification =
                getOrCreateDraftVerification(driver);

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

        return toResponse(updatedDriver, verification);
    }


    // Submit the current DRAFT verification for administrator review
    public DriverResponse submitForVerification(String keycloakUserId) {

        Account account = getActiveAccount(keycloakUserId);

        Driver driver = driverRepository
                .findByAccount_Id(account.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver profile not found."
                        )
                );

        // Retrieve the latest verification attempt.
        DriverVerification verification =
                getLatestVerification(driver);

        // Only a DRAFT application can be submitted.
        if (verification.getStatus() != DriverVerificationStatus.DRAFT) {

            throw new ConflictException(
                    "Your driver application cannot be submitted in its current status."
            );
        }

        // Validate the driver's profile information.
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

        // Check whether a licence document has been uploaded
        // for this particular verification attempt.
        validateDocumentUploaded(verification);

        // Check licence expiry.
        validateLicenseNotExpired(driver);

        verification.setStatus(DriverVerificationStatus.PENDING);
        verification.setSubmittedAt(Instant.now());

        DriverVerification submittedVerification =
                driverVerificationRepository.save(verification);

        return toResponse(driver, submittedVerification);
    }


    // =========================================================
    // DRIVER DOCUMENT OPERATIONS
    // =========================================================


    // Upload or replace a driving licence document
    public DriverLicenseDocumentResponse uploadMyLicenseDocument(
            String keycloakUserId,
            MultipartFile file) {

        Account account = getActiveAccount(keycloakUserId);

        Driver driver = driverRepository
                .findByAccount_Id(account.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver profile not found."
                        )
                );

        // Get the editable verification.
        // If the latest one is REJECTED, create a new DRAFT attempt.
        DriverVerification verification =
                getOrCreateDraftVerification(driver);

        // Upload the new file to RustFS.
        DriverLicenseStorageService.UploadedLicense uploaded =
                driverLicenseStorageService.upload(
                        driver.getId(),
                        file
                );

        // Keep the previous object key in case the driver
        // is replacing a document within the same DRAFT attempt.
        String previousDocumentKey =
                verification.getLicenseDocumentKey();

        Instant uploadedAt = Instant.now();

        // Update document metadata on DriverVerification.
        verification.setLicenseDocumentKey(uploaded.objectKey());

        verification.setLicenseDocumentFileName(
                uploaded.fileName()
        );

        verification.setLicenseDocumentContentType(
                uploaded.contentType()
        );

        verification.setLicenseDocumentSize(
                uploaded.size()
        );

        verification.setLicenseDocumentUploadedAt(uploadedAt);

        try {

            driverVerificationRepository.save(verification);

        } catch (RuntimeException exception) {

            // If saving the new document reference fails,
            // clean up the newly uploaded object.
            driverLicenseStorageService.deleteQuietly(
                    uploaded.objectKey()
            );

            throw exception;
        }

        // Coordinate RustFS cleanup with the PostgreSQL transaction.
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {

                        // Delete the old document only after
                        // the new document reference is committed.
                        if (previousDocumentKey != null
                                && !previousDocumentKey.isBlank()
                                && !previousDocumentKey.equals(
                                uploaded.objectKey()
                        )) {

                            driverLicenseStorageService.deleteQuietly(
                                    previousDocumentKey
                            );
                        }
                    }

                    @Override
                    public void afterCompletion(int status) {

                        // If the database transaction rolls back,
                        // delete the newly uploaded object.
                        if (status != STATUS_COMMITTED) {

                            driverLicenseStorageService.deleteQuietly(
                                    uploaded.objectKey()
                            );
                        }
                    }
                }
        );

        return new DriverLicenseDocumentResponse(
                uploaded.fileName(),
                uploaded.contentType(),
                uploaded.size(),
                uploadedAt,
                "Licence document uploaded successfully."
        );
    }


    // =========================================================
    // ADMINISTRATOR OPERATIONS
    // =========================================================


    // Retrieve verification applications, optionally filtered by status.
    //
    // Each result represents one verification attempt.
    // Historical rejected applications are retained and can also appear.
    @Transactional(readOnly = true)
    public Page<DriverResponse> getDrivers(
            String status,
            Pageable pageable) {

        DriverVerificationStatus verificationStatus =
                parseVerificationStatus(status);

        Page<DriverVerification> verifications;

        if (verificationStatus == null) {

            verifications = driverVerificationRepository.findAll(pageable);

        } else {

            verifications =
                    driverVerificationRepository.findAllByStatus(
                            verificationStatus,
                            pageable
                    );
        }

        return verifications.map(
                verification -> toResponse(
                        verification.getDriver(),
                        verification
                )
        );
    }


    // Retrieve a particular driver's profile with its latest verification
    @Transactional(readOnly = true)
    public DriverResponse getDriver(UUID driverId) {

        Driver driver = getDriverEntity(driverId);

        DriverVerification verification =
                getLatestVerification(driver);

        return toResponse(driver, verification);
    }


    // Approve the latest pending verification
    public DriverResponse approveDriver(
            UUID driverId,
            String adminKeycloakUserId) {

        Driver driver = getDriverEntity(driverId);

        Account administrator = getActiveAccount(
                adminKeycloakUserId
        );

        validateReviewer(driver, administrator);

        DriverVerification verification =
                getLatestVerification(driver);

        validatePendingStatus(verification);

        validateLicenseNotExpired(driver);

        validateDocumentUploaded(verification);

        verification.setStatus(
                DriverVerificationStatus.APPROVED
        );

        verification.setReviewedAt(Instant.now());

        verification.setReviewedBy(administrator);

        verification.setRejectionReason(null);

        DriverVerification approvedVerification =
                driverVerificationRepository.save(verification);

        return toResponse(driver, approvedVerification);
    }


    // Reject the latest pending verification
    public DriverResponse rejectDriver(
            UUID driverId,
            String adminKeycloakUserId,
            String reason) {

        if (reason == null || reason.isBlank()) {

            throw new BadRequestException(
                    "A rejection reason is required."
            );
        }

        Driver driver = getDriverEntity(driverId);

        Account administrator = getActiveAccount(
                adminKeycloakUserId
        );

        validateReviewer(driver, administrator);

        DriverVerification verification =
                getLatestVerification(driver);

        validatePendingStatus(verification);

        verification.setStatus(
                DriverVerificationStatus.REJECTED
        );

        verification.setRejectionReason(
                reason.trim()
        );

        verification.setReviewedAt(Instant.now());

        verification.setReviewedBy(administrator);

        DriverVerification rejectedVerification =
                driverVerificationRepository.save(verification);

        return toResponse(driver, rejectedVerification);
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
                                "Account not found."
                        )
                );

        if (!account.isActive()) {

            throw new AccessDeniedException(
                    "Inactive accounts cannot perform this operation."
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
                                "Driver profile not found."
                        )
                );
    }


    // Retrieve the latest verification attempt for a driver
    private DriverVerification getLatestVerification(Driver driver) {

        return driverVerificationRepository
                .findFirstByDriver_IdOrderByCreatedAtDescIdDesc(
                        driver.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver verification record not found."
                        )
                );
    }


    // Return an existing DRAFT verification or create a new one
    // when the latest verification was REJECTED.
    private DriverVerification getOrCreateDraftVerification(
            Driver driver) {

        var latestOptional = driverVerificationRepository
                .findFirstByDriver_IdOrderByCreatedAtDescIdDesc(
                        driver.getId()
                );

        if (latestOptional.isEmpty()) {

            return createDraftVerification(driver);
        }

        DriverVerification latest = latestOptional.get();

        if (latest.getStatus() == DriverVerificationStatus.DRAFT) {

            return latest;
        }

        if (latest.getStatus() == DriverVerificationStatus.REJECTED) {

            // Preserve the rejected verification.
            // Start a completely new application attempt.

            return createDraftVerification(driver);
        }

        throw new ConflictException(
                "Your driver application cannot be modified in its current status."
        );
    }


    // Create a new DRAFT verification attempt
    private DriverVerification createDraftVerification(Driver driver) {

        DriverVerification verification = new DriverVerification();

        verification.setDriver(driver);

        verification.setStatus(
                DriverVerificationStatus.DRAFT
        );

        return driverVerificationRepository.save(verification);
    }


    // Apply editable profile information
    private void applyProfileInformation(
            Driver driver,
            DriverProfileRequest request,
            String licenseNumber,
            String issuingCountry) {

        driver.setLicenseNumber(licenseNumber);

        driver.setLicenseClass(
                request.licenseClass()
                        .trim()
                        .toUpperCase(Locale.ROOT)
        );

        driver.setLicenseIssuingCountry(issuingCountry);

        driver.setLicenseExpiryDate(
                request.licenseExpiryDate()
        );
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
                    "This driving licence is already registered."
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


    // Ensure that a verification has an uploaded document
    private void validateDocumentUploaded(
            DriverVerification verification) {

        if (verification.getLicenseDocumentKey() == null
                || verification.getLicenseDocumentKey().isBlank()) {

            throw new BadRequestException(
                    "Please upload your licence document before submitting."
            );
        }
    }


    // Prevent submission or approval of expired licences
    private void validateLicenseNotExpired(Driver driver) {

        if (driver.getLicenseExpiryDate() == null
                || driver.getLicenseExpiryDate().isBefore(LocalDate.now())) {

            throw new BadRequestException(
                    "The driving licence has expired or has no valid expiry date."
            );
        }
    }


    // Ensure the verification is waiting for administrator review
    private void validatePendingStatus(
            DriverVerification verification) {

        if (verification.getStatus()
                != DriverVerificationStatus.PENDING) {

            throw new ConflictException(
                    "Only PENDING verification applications can be reviewed."
            );
        }
    }


    // Prevent an administrator from reviewing their own driver profile
    private void validateReviewer(
            Driver driver,
            Account administrator) {

        if (driver.getAccount().getId().equals(administrator.getId())) {

            throw new AccessDeniedException(
                    "Administrators cannot review their own driver profiles."
            );
        }
    }


    // Parse and validate an optional verification status filter
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
                    "Invalid verification status. Allowed values: DRAFT, PENDING, APPROVED, REJECTED."
            );
        }
    }


    // =========================================================
    // RESPONSE MAPPING
    // =========================================================


    // Convert a Driver entity and its latest verification into DriverResponse
    private DriverResponse toResponse(Driver driver) {

        DriverVerification verification =
                driverVerificationRepository
                        .findFirstByDriver_IdOrderByCreatedAtDescIdDesc(
                                driver.getId()
                        )
                        .orElse(null);

        return toResponse(driver, verification);
    }


    // Convert Driver and a specific verification attempt into DriverResponse
    private DriverResponse toResponse(
            Driver driver,
            DriverVerification verification) {

        return new DriverResponse(

                driver.getId(),

                driver.getLicenseNumber(),

                driver.getLicenseClass(),

                driver.getLicenseIssuingCountry(),

                driver.getLicenseExpiryDate(),

                verification != null
                        ? toVerificationResponse(verification)
                        : null,

                driver.getCreatedAt(),

                driver.getUpdatedAt()
        );
    }


    // Convert DriverVerification entity into DriverVerificationResponse
    private DriverVerificationResponse toVerificationResponse(
            DriverVerification verification) {

        return new DriverVerificationResponse(

                verification.getId(),

                verification.getStatus(),

                verification.getSubmittedAt(),

                verification.getReviewedAt(),

                verification.getRejectionReason(),

                verification.getLicenseDocumentKey() != null
                        && !verification.getLicenseDocumentKey().isBlank(),

                verification.getLicenseDocumentFileName(),

                verification.getLicenseDocumentContentType(),

                verification.getLicenseDocumentSize(),

                verification.getLicenseDocumentUploadedAt(),

                verification.getCreatedAt()
        );
    }
}
