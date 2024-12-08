package org.synyx.urlaubsverwaltung.tenancy.tenant;

interface TenantAware {

    String getTenantId();

    void setTenantId(String tenantId);
}
