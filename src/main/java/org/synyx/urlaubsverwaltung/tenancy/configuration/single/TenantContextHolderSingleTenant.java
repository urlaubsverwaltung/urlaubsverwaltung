package org.synyx.urlaubsverwaltung.tenancy.configuration.single;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.tenant.TenantContextHolder;
import org.synyx.urlaubsverwaltung.tenancy.tenant.TenantId;

import java.util.Optional;


@Component
@Conditional(IsSingleTenantMode.class)
class TenantContextHolderSingleTenant implements TenantContextHolder {

    private final String defaultTenantId;

    TenantContextHolderSingleTenant(SingleTenantConfigurationProperties singleTenantConfigurationProperties) {
        this.defaultTenantId = singleTenantConfigurationProperties.getDefaultTenantId();
    }

    @Override
    public Optional<TenantId> getCurrentTenantId() {
        return Optional.of(new TenantId(defaultTenantId));
    }

}
