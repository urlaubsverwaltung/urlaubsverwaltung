package org.synyx.urlaubsverwaltung.extension.tenancy;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("uv.extensions.tenancy")
public class TenancyConfigurationProperties {

    private String tenantId = "default";

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
