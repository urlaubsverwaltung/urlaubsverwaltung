package org.synyx.urlaubsverwaltung.extension.tenancy;

import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.DefaultTenantSupplier;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

@Configuration
@ConditionalOnProperty(value = "uv.extensions.enabled", havingValue = "true")
@ConditionalOnSingleTenantMode
@EnableConfigurationProperties(TenancyConfigurationProperties.class)
public class TenancyConfiguration {

    private final TenancyConfigurationProperties tenancyConfigurationProperties;

    public TenancyConfiguration(TenancyConfigurationProperties tenancyConfigurationProperties) {
        this.tenancyConfigurationProperties = tenancyConfigurationProperties;
    }

    @Bean
    TenantSupplier tenantSupplierExtension() {
        return new DefaultTenantSupplier(tenancyConfigurationProperties.getTenantId());
    }
}
