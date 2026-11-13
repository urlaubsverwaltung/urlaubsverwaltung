package org.synyx.urlaubsverwaltung.security.oidc;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
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
    public @Nullable GrantedAuthority convert(@NonNull String source) {
        final String rolePrefix = properties.getRolePrefix();
        if (!source.startsWith(rolePrefix)) {
            return null;
        }

        try {
            final String authority = source.substring(rolePrefix.length()).toUpperCase();
            return new SimpleGrantedAuthority(Role.valueOf(authority).name());
        } catch (IllegalArgumentException _) {
            return null;
        }
    }
}
