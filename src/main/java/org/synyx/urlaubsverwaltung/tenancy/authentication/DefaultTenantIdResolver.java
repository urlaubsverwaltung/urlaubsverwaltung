package org.synyx.urlaubsverwaltung.tenancy.authentication;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.SingleTenantConfigurationProperties;
import org.synyx.urlaubsverwaltung.tenancy.tenant.TenantId;

import java.util.Optional;

@Component
@ConditionalOnSingleTenantMode
class DefaultTenantIdResolver implements TenantIdResolver {

    private final TenantId defaultTenantId;

    DefaultTenantIdResolver(SingleTenantConfigurationProperties properties) {
        this.defaultTenantId = new TenantId(properties.getDefaultTenantId());
    }

    @Override
    public Optional<TenantId> resolve(OAuth2AuthenticationToken token) {
        return Optional.of(defaultTenantId);
    }

    @Override
    public Optional<TenantId> resolve(OAuth2LoginAuthenticationToken token) {
        return Optional.of(defaultTenantId);
    }

    @Override
    public Optional<TenantId> resolve(OidcUserAuthority authority) {
        return Optional.of(defaultTenantId);
    }
}
