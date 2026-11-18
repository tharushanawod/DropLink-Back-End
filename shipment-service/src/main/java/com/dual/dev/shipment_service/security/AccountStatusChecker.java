package com.dual.dev.shipment_service.security;

import com.dual.dev.shipment_service.account.client.AccountServiceClient;
import com.dual.dev.shipment_service.account.dto.AccountStatusResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component("accountStatusChecker")
@RequiredArgsConstructor
public class AccountStatusChecker {

    private final AccountServiceClient accountServiceClient;

    // Check whether the authenticated user's account is active
    public boolean isActive(Authentication authentication) {

        Jwt jwt = getJwt(authentication);

        if (jwt == null) {
            return false;
        }

        try {

            AccountStatusResponse response =
                    accountServiceClient.getCurrentAccountStatus(
                            jwt.getTokenValue()
                    );

            return response != null && response.active();

        } catch (RestClientException ex) {

            // Fail closed if Account Service cannot verify the account
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to verify account status",
                    ex
            );
        }
    }

    // Extract authenticated user's JWT
    private Jwt getJwt(Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof Jwt jwt)) {

            return null;
        }

        if (jwt.getSubject() == null ||
                jwt.getSubject().isBlank()) {

            return null;
        }

        return jwt;
    }
}