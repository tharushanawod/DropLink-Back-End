package com.dual.dev.account_service.branch_staff.entity;

import com.dual.dev.account_service.account.entity.Account;
import com.dual.dev.account_service.branch.entity.Branch;

import com.dual.dev.account_service.branch_staff.enums.BranchStaffDesignation;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "branch_staff",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_branch_staff_account",
                        columnNames = "account_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_branch_staff_branch",
                        columnList = "branch_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class BranchStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // One account can have one branch staff assignment.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "account_id",
            nullable = false,
            unique = true
    )
    private Account account;

    // Multiple staff members can belong to one branch.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "branch_id",
            nullable = false
    )
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private BranchStaffDesignation designation;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(name = "deactivated_at")
    private Instant deactivatedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {

        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;

        if (assignedAt == null) {
            assignedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
