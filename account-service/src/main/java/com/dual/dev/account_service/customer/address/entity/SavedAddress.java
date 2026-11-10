package com.dual.dev.account_service.customer.address.entity;

import com.dual.dev.account_service.account.entity.Account;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "saved_addresses",
        indexes = {
                @Index(
                        name = "idx_saved_addresses_account_id",
                        columnList = "account_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class SavedAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    // Account that owns this saved address
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;


    @Column(nullable = false, length = 50)
    private String label;


    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;


    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;


    @Column(name = "address_line_1", nullable = false, length = 255)
    private String addressLine1;


    @Column(name = "address_line_2", length = 255)
    private String addressLine2;


    @Column(nullable = false, length = 100)
    private String city;


    @Column(length = 100)
    private String district;


    @Column(name = "postal_code", length = 20)
    private String postalCode;


    @Column(nullable = false, length = 100)
    private String country;


    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;


    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;


    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress = false;


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