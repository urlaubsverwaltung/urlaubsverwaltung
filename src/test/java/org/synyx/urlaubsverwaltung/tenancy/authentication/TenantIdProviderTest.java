package org.synyx.urlaubsverwaltung.tenancy.authentication;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.synyx.urlaubsverwaltung.tenancy.tenant.TenantId;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TenantIdProviderTest {

    @Test
    void returnsEmptyWhenNoResolvers() {
        final TenantIdProvider provider = new TenantIdProvider(List.of());
        assertThat(provider.resolve(mock(OAuth2AuthenticationToken.class))).isEmpty();
        assertThat(provider.resolve(mock(OAuth2LoginAuthenticationToken.class))).isEmpty();
        assertThat(provider.resolve(mock(OidcUserAuthority.class))).isEmpty();
    }

    @Test
    void returnsFirstNonEmptyResultFromOAuth2AuthenticationTokenChain() {
        final TenantIdResolver empty = new TenantIdResolver() {
            @Override
            public Optional<TenantId> resolve(OAuth2AuthenticationToken token) {
                return Optional.empty();
            }
        };
        final TenantIdResolver second = new TenantIdResolver() {
            @Override
            public Optional<TenantId> resolve(OAuth2AuthenticationToken token) {
                return Optional.of(new TenantId("second"));
            }
        };
        final TenantIdResolver third = new TenantIdResolver() {
            @Override
            public Optional<TenantId> resolve(OAuth2AuthenticationToken token) {
                return Optional.of(new TenantId("third"));
            }
        };

        final TenantIdProvider provider = new TenantIdProvider(List.of(empty, second, third));

        assertThat(provider.resolve(mock(OAuth2AuthenticationToken.class)))
            .contains(new TenantId("second"));
    }

    @Test
    void returnsEmptyWhenAllResolversReturnEmpty() {
        final TenantIdResolver empty1 = new TenantIdResolver() {};
        final TenantIdResolver empty2 = new TenantIdResolver() {};

        final TenantIdProvider provider = new TenantIdProvider(List.of(empty1, empty2));

        assertThat(provider.resolve(mock(OAuth2AuthenticationToken.class))).isEmpty();
    }

    @Test
    void returnsFirstNonEmptyResultFromOAuth2LoginAuthenticationTokenChain() {
        final TenantIdResolver empty = new TenantIdResolver() {};
        final TenantIdResolver hit = new TenantIdResolver() {
            @Override
            public Optional<TenantId> resolve(OAuth2LoginAuthenticationToken token) {
                return Optional.of(new TenantId("login"));
            }
        };

        final TenantIdProvider provider = new TenantIdProvider(List.of(empty, hit));

        assertThat(provider.resolve(mock(OAuth2LoginAuthenticationToken.class)))
            .contains(new TenantId("login"));
    }

    @Test
    void returnsFirstNonEmptyResultFromOidcUserAuthorityChain() {
        final TenantIdResolver empty = new TenantIdResolver() {};
        final TenantIdResolver hit = new TenantIdResolver() {
            @Override
            public Optional<TenantId> resolve(OidcUserAuthority authority) {
                return Optional.of(new TenantId("oidc"));
            }
        };

        final TenantIdProvider provider = new TenantIdProvider(List.of(empty, hit));

        assertThat(provider.resolve(mock(OidcUserAuthority.class)))
            .contains(new TenantId("oidc"));
    }
}
