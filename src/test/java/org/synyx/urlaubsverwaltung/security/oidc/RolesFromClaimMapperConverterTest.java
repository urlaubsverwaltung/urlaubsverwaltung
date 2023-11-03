package org.synyx.urlaubsverwaltung.security.oidc;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

class RolesFromClaimMapperConverterTest {

    @Test
    void ensureToConvertStringToGrantedAuthority() {
        final RolesFromClaimMapperConverter converter = new RolesFromClaimMapperConverter(new RolesFromClaimMappersProperties());
        final GrantedAuthority grantedAuthority = converter.convert("urlaubsverwaltung_user");
        assertThat(grantedAuthority.getAuthority()).isEqualTo("USER");
    }

    @Test
    void ensureToConvertStringToGrantedAuthorityWithDifferentRolePrefix() {
        final RolesFromClaimMappersProperties properties = new RolesFromClaimMappersProperties();
        properties.setRolePrefix("someprefix_");

        final RolesFromClaimMapperConverter converter = new RolesFromClaimMapperConverter(properties);
        final GrantedAuthority grantedAuthority = converter.convert("someprefix_user");
        assertThat(grantedAuthority.getAuthority()).isEqualTo("USER");
    }

    @Test
    void ensureToReturnNullIfNotARole() {
        final RolesFromClaimMapperConverter converter = new RolesFromClaimMapperConverter(new RolesFromClaimMappersProperties());
        final GrantedAuthority grantedAuthority = converter.convert("urlaubsverwaltung_NotARole");
        assertThat(grantedAuthority).isNull();
    }
}
