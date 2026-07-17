package org.synyx.urlaubsverwaltung.tenancy.authentication;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.synyx.urlaubsverwaltung.tenancy.tenant.TenantId;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class TenantIdProvider {

    private final List<TenantIdResolver> resolvers;

    public TenantIdProvider(List<TenantIdResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public Optional<TenantId> resolve(OAuth2AuthenticationToken token) {
        return first(resolver -> resolver.resolve(token));
    }

    public Optional<TenantId> resolve(OAuth2LoginAuthenticationToken token) {
        return first(resolver -> resolver.resolve(token));
    }

    public Optional<TenantId> resolve(OidcUserAuthority authority) {
        return first(resolver -> resolver.resolve(authority));
    }

    private Optional<TenantId> first(Function<TenantIdResolver, Optional<TenantId>> fn) {
        return resolvers.stream()
            .map(fn)
            .flatMap(Optional::stream)
            .findFirst();
    }
}
