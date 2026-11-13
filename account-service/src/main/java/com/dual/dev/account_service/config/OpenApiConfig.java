package com.dual.dev.account_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.Scopes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accountOpenAPI() {

        String securitySchemeName = "keycloakOAuth";

        return new OpenAPI()

                .info(new Info()
                        .title("DropLink Account Service API")
                        .description("APIs for managing DropLink accounts and customer profiles")
                        .version("1.0.0")
                )

                // Apply Keycloak authentication to documented operations
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(
                                        securitySchemeName,
                                        List.of("openid", "profile", "email")
                                )
                )

                // Define the OAuth2 security scheme
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,

                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.OAUTH2)
                                                .description("Authenticate using Keycloak")

                                                .flows(
                                                        new OAuthFlows()

                                                                .authorizationCode(
                                                                        new OAuthFlow()

                                                                                // Keycloak authorization endpoint
                                                                                .authorizationUrl(
                                                                                        "http://localhost:8180/realms/droplink/protocol/openid-connect/auth"
                                                                                )

                                                                                // Keycloak token endpoint
                                                                                .tokenUrl(
                                                                                        "http://localhost:8180/realms/droplink/protocol/openid-connect/token"
                                                                                )

                                                                                // Requested OAuth scopes
                                                                                .scopes(
                                                                                        new Scopes()
                                                                                                .addString("openid", "OpenID Connect")
                                                                                                .addString("profile", "User profile")
                                                                                                .addString("email", "User email")
                                                                                )
                                                                )
                                                )
                                )
                );
    }
}