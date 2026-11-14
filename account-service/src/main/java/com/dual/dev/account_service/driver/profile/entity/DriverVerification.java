
package com.dual.dev.account_service.driver.profile.entity;

import com.dual.dev.account_service.account.entity.Account;

import com.dual.dev.account_service.driver.profile.enums.DriverVerificationStatus;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "driver_verifications",
        indexes = {
                @Index(
                        name = "idx_driver_verification_driver",
                        columnList = "driver_id, created_at"
                ),
                @Index(
                        name = "idx_driver_verification_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class DriverVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Multiple verification attempts can belong to one driver.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DriverVerificationStatus status = DriverVerificationStatus.DRAFT;

    private Instant submittedAt;

    private Instant reviewedAt;

    @Column(length = 500)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_account_id")
    private Account reviewedBy;

    // Licence document information for this application attempt.
    @Column(name = "license_document_key", length = 500)
    private String licenseDocumentKey;

    @Column(name = "license_document_file_name", length = 255)
    private String licenseDocumentFileName;

    @Column(name = "license_document_content_type", length = 100)
    private String licenseDocumentContentType;

    @Column(name = "license_document_size")
    private Long licenseDocumentSize;

    @Column(name = "license_document_uploaded_at")
    private Instant licenseDocumentUploadedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = DriverVerificationStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
