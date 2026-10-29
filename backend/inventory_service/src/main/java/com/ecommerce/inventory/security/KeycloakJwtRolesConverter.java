package com.ecommerce.inventory.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakJwtRolesConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final Logger log =
            LoggerFactory.getLogger(KeycloakJwtRolesConverter.class);

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        log.debug("========== KEYCLOAK ROLE DEBUG ==========");
        log.debug("JWT subject: {}", jwt.getSubject());
        log.debug("JWT issuer: {}", jwt.getIssuer());

        Map<String, Object> realmAccess =
                jwt.getClaim("realm_access");

        log.debug("realm_access: {}", realmAccess);

        if (realmAccess == null) {
            log.warn("realm_access is NULL");
            log.debug("=========================================");
            return Collections.emptyList();
        }

        Object roles = realmAccess.get("roles");

        log.debug("realm_access.roles: {}", roles);

        if (!(roles instanceof Collection<?> roleCollection)) {
            log.warn("realm_access.roles is not a Collection");
            log.debug("=========================================");
            return Collections.emptyList();
        }

        Collection<GrantedAuthority> authorities =
                roleCollection.stream()
                        .map(Object::toString)
                        .map(role -> {
                            String authority = "ROLE_" + role;

                            log.debug(
                                    "Keycloak role '{}' -> Spring authority '{}'",
                                    role,
                                    authority
                            );

                            return new SimpleGrantedAuthority(authority);
                        })
                        .collect(Collectors.toList());

        log.debug("Final authorities: {}", authorities);
        log.debug("=========================================");

        return authorities;
    }
}