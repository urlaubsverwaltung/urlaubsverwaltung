package org.synyx.urlaubsverwaltung.tenancy.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;

@MappedSuperclass
@EntityListeners(TenantListener.class)
public abstract class AbstractTenantAwareEntity implements TenantAware, Serializable {

    @Serial
    private static final long serialVersionUID = 8093106407703011072L;

    @Size(max = 255)
    @Column(name = "tenant_id")
    private String tenantId;

    protected AbstractTenantAwareEntity() {
        // OK
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
