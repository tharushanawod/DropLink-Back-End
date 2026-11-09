
package com.dual.dev.account_service.account.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(

        UUID id,

        String email,

        String firstName,

        String lastName,

        String phoneNumber,

        boolean active,

        Instant createdAt,

        Instant updatedAt

) {
}
