package com.dual.dev.account_service.driver.profile.dto.response;
import com.dual.dev.account_service.driver.profile.enums.DriverVerificationStatus;

import java.time.Instant;
import java.util.UUID;

public record DriverVerificationResponse(

        UUID id,

        DriverVerificationStatus status,

        Instant submittedAt,

        Instant reviewedAt,

        String rejectionReason,

        boolean documentUploaded,

        String documentFileName,

        String documentContentType,

        Long documentSize,

        Instant documentUploadedAt,

        Instant createdAt

) {
}
