package org.synyx.urlaubsverwaltung.tenancy.authentication;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.SingleTenantConfigurationProperties;
import org.synyx.urlaubsverwaltung.tenancy.tenant.TenantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DefaultTenantIdResolverTest {

    @Test
    void resolvesConfiguredDefaultTenantIdForAllOverloads() {
        final DefaultTenantIdResolver sut = new DefaultTenantIdResolver(properties("acme"));

        assertThat(sut.resolve(mock(OAuth2AuthenticationToken.class))).contains(new TenantId("acme"));
        assertThat(sut.resolve(mock(OAuth2LoginAuthenticationToken.class))).contains(new TenantId("acme"));
        assertThat(sut.resolve(mock(OidcUserAuthority.class))).contains(new TenantId("acme"));
    }

    @Test
    void resolvesTheDefaultValueWhenPropertyIsUnset() {
        final DefaultTenantIdResolver sut = new DefaultTenantIdResolver(new SingleTenantConfigurationProperties());

        assertThat(sut.resolve(mock(OAuth2AuthenticationToken.class))).contains(new TenantId("default"));
        assertThat(sut.resolve(mock(OAuth2LoginAuthenticationToken.class))).contains(new TenantId("default"));
        assertThat(sut.resolve(mock(OidcUserAuthority.class))).contains(new TenantId("default"));
    }

    private static SingleTenantConfigurationProperties properties(String defaultTenantId) {
        final SingleTenantConfigurationProperties props = new SingleTenantConfigurationProperties();
        props.setDefaultTenantId(defaultTenantId);
        return props;
    }
}
