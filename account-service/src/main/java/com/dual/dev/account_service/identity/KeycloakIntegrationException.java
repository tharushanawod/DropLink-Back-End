package com.dual.dev.account_service.identity;

public class KeycloakIntegrationException extends RuntimeException {

    public KeycloakIntegrationException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}
