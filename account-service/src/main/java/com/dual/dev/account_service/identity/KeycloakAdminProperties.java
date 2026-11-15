package com.dual.dev.account_service.identity;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.keycloak.admin")
public record KeycloakAdminProperties(

        String serverUrl,

        String realm,

        String clientId,

        String clientSecret

) {
}
