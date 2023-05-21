package org.synyx.urlaubsverwaltung.extension.tenancy;

import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.DefaultTenantSupplier;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@ConditionalOnProperty(value = "uv.extensions.enabled", havingValue = "true")
@EnableConfigurationProperties(TenancyConfigurationProperties.class)
public class TenancyConfiguration {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final TenancyConfigurationProperties tenancyConfigurationProperties;

    public TenancyConfiguration(TenancyConfigurationProperties tenancyConfigurationProperties) {
        this.tenancyConfigurationProperties = tenancyConfigurationProperties;
    }

    @Bean
    TenantSupplier tenantSupplierExtension() {
        return new DefaultTenantSupplier(tenancyConfigurationProperties.getTenantId());
    }


}
