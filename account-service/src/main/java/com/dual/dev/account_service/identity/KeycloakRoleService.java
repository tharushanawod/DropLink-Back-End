
package com.dual.dev.account_service.identity;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.ParameterizedTypeReference;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import org.springframework.stereotype.Service;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class KeycloakRoleService {

    private final RestClient restClient;

    private final KeycloakAdminProperties properties;

    public KeycloakRoleService(KeycloakAdminProperties properties) {

        this.properties = properties;

        // Create RestClient directly.
        // No RestClient.Builder bean injection is required.
        this.restClient = RestClient.create(properties.serverUrl());
    }


    // Add a realm role to an existing Keycloak user
    public void addRealmRole(String keycloakUserId, String roleName) {

        String accessToken = getAdminAccessToken();

        KeycloakRoleRepresentation role =
                getRealmRole(accessToken, roleName);

        try {

            restClient.post()
                    .uri(
                            "/admin/realms/{realm}/users/{userId}/role-mappings/realm",
                            properties.realm(),
                            keycloakUserId
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers ->
                            headers.setBearerAuth(accessToken)
                    )
                    .body(List.of(role))
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException exception) {

            throw new KeycloakIntegrationException(
                    "Unable to assign the " + roleName + " role in Keycloak.",
                    exception
            );
        }
    }


    // Remove a realm role from an existing Keycloak user
    public void removeRealmRole(String keycloakUserId, String roleName) {

        String accessToken = getAdminAccessToken();

        KeycloakRoleRepresentation role =
                getRealmRole(accessToken, roleName);

        try {

            restClient.method(HttpMethod.DELETE)
                    .uri(
                            "/admin/realms/{realm}/users/{userId}/role-mappings/realm",
                            properties.realm(),
                            keycloakUserId
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers ->
                            headers.setBearerAuth(accessToken)
                    )
                    .body(List.of(role))
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException exception) {

            throw new KeycloakIntegrationException(
                    "Unable to remove the " + roleName + " role in Keycloak.",
                    exception
            );
        }
    }


    // Obtain a service-account access token using client credentials
    private String getAdminAccessToken() {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());

        try {

            Map<String, Object> response = restClient.post()
                    .uri(
                            "/realms/{realm}/protocol/openid-connect/token",
                            properties.realm()
                    )
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null
                    || response.get("access_token") == null) {

                throw new IllegalStateException(
                        "Keycloak did not return an admin access token."
                );
            }

            return response.get("access_token").toString();

        } catch (RestClientException exception) {

            throw new KeycloakIntegrationException(
                    "Unable to authenticate with the Keycloak administration API.",
                    exception
            );
        }
    }


    // Retrieve the realm role representation required by the mapping API
    private KeycloakRoleRepresentation getRealmRole(
            String accessToken,
            String roleName) {

        try {

            return restClient.get()
                    .uri(
                            "/admin/realms/{realm}/roles/{roleName}",
                            properties.realm(),
                            roleName
                    )
                    .headers(headers ->
                            headers.setBearerAuth(accessToken)
                    )
                    .retrieve()
                    .body(KeycloakRoleRepresentation.class);

        } catch (RestClientException exception) {

            throw new KeycloakIntegrationException(
                    "Unable to find the Keycloak realm role: " + roleName,
                    exception
            );
        }
    }


    // Only the fields required for role mapping are needed.
    // Other fields returned by Keycloak are ignored by Jackson.
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private record KeycloakRoleRepresentation(
            String id,
            String name
    ) {
    }
}
