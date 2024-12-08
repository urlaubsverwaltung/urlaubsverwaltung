package org.synyx.urlaubsverwaltung.tenancy.configuration.single;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("uv.tenant.single")
public class SingleTenantConfigurationProperties {

    /**
     * Sets the id of the default tenant, default is 'default'
     */
    @NotBlank
    @Size(min = 1, max = 255)
    private String defaultTenantId = "default";

    public String getDefaultTenantId() {
        return defaultTenantId;
    }

    public void setDefaultTenantId(String defaultTenantId) {
        this.defaultTenantId = defaultTenantId;
    }
}
