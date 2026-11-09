package com.dual.dev.account_service.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Component
public class KeycloakRoleConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final JwtGrantedAuthoritiesConverter defaultConverter =
            new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        Collection<GrantedAuthority> authorities =
                new ArrayList<>();

        // 1. Extract the default scope authorities
        Collection<GrantedAuthority> scopeAuthorities =
                defaultConverter.convert(jwt);

        if (scopeAuthorities != null) {
            authorities.addAll(scopeAuthorities);
        }

        // 2. Get the realm_access claim
        Map<String, Object> realmAccess =
                jwt.getClaim("realm_access");

        if (realmAccess == null) {
            return authorities;
        }

        // 3. Get the roles inside realm_access
        Object rolesObject = realmAccess.get("roles");

        if (rolesObject instanceof Collection<?> roles) {

            // 4. Convert each Keycloak role into a Spring authority
            for (Object role : roles) {

                if (role instanceof String roleName
                        && !roleName.isBlank()) {

                    authorities.add(
                            new SimpleGrantedAuthority(
                                    "ROLE_" + roleName
                            )
                    );
                }
            }
        }

        return authorities;
    }
}