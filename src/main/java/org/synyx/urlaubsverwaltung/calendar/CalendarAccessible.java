package org.synyx.urlaubsverwaltung.calendar;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Inheritance
public abstract class CalendarAccessible extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "calendar_accessible_generator")
    @SequenceGenerator(name = "calendar_accessible_generator", sequenceName = "calendar_accessible_id_seq")
    private Long id;

    private boolean isAccessible = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isAccessible() {
        return isAccessible;
    }

    public void setAccessible(boolean accessible) {
        isAccessible = accessible;
    }
}
