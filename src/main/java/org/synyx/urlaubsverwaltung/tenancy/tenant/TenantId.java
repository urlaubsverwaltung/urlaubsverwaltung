package org.synyx.urlaubsverwaltung.tenancy.tenant;


import org.springframework.util.StringUtils;

public record TenantId(String tenantId) {
    public boolean valid() {
        return StringUtils.hasText(tenantId) && tenantId.length() <= 255;
    }
}
