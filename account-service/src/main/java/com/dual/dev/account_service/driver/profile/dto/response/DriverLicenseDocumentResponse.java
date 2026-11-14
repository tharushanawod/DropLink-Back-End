package com.dual.dev.account_service.driver.profile.dto.response;

import java.time.Instant;

public record DriverLicenseDocumentResponse(

        String fileName,

        String contentType,

        Long size,

        Instant uploadedAt,

        String message

) {
}