package com.dual.dev.account_service.driver.profile.entity;

import com.dual.dev.account_service.account.entity.Account;

import com.dual.dev.account_service.driver.profile.enums.DriverVerificationStatus;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "drivers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_driver_account",
                        columnNames = "account_id"
                ),
                @UniqueConstraint(
                        name = "uk_driver_license_country",
                        columnNames = {
                                "license_number",
                                "license_issuing_country"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    // Account associated with this driver profile
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "account_id",
            nullable = false,
            unique = true
    )
    private Account account;


    @Column(name = "license_number", nullable = false, length = 50)
    private String licenseNumber;


    @Column(name = "license_class", nullable = false, length = 50)
    private String licenseClass;


    @Column(
            name = "license_issuing_country",
            nullable = false,
            length = 100
    )
    private String licenseIssuingCountry;


    @Column(name = "license_expiry_date", nullable = false)
    private LocalDate licenseExpiryDate;


    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private DriverVerificationStatus verificationStatus =
            DriverVerificationStatus.DRAFT;


    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;


    @Column(name = "submitted_at")
    private Instant submittedAt;


    @Column(name = "reviewed_at")
    private Instant reviewedAt;


    // Account of the administrator who reviewed the driver
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_account_id")
    private Account reviewedBy;


    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    @PrePersist
    protected void onCreate() {

        Instant now = Instant.now();

        createdAt = now;

        updatedAt = now;
    }


    @PreUpdate
    protected void onUpdate() {

        updatedAt = Instant.now();
    }
}