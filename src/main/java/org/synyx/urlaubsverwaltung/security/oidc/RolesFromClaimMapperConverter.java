package org.synyx.urlaubsverwaltung.security.oidc;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.person.Role;

@Component
class RolesFromClaimMapperConverter implements Converter<String, GrantedAuthority> {

    private final RolesFromClaimMappersProperties properties;

    RolesFromClaimMapperConverter(RolesFromClaimMappersProperties properties) {
        this.properties = properties;
    }

    @Override
    public GrantedAuthority convert(@NonNull String source) {
        try {
            final String authority = source.replace(properties.getRolePrefix(), "").toUpperCase();
            return new SimpleGrantedAuthority(Role.valueOf(authority).name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
