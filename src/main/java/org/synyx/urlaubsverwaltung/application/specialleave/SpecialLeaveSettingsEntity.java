package org.synyx.urlaubsverwaltung.application.specialleave;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity(name = "special_leave_settings")
public class SpecialLeaveSettingsEntity extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "special_leaves_settings_generator")
    @SequenceGenerator(name = "special_leaves_settings_generator", sequenceName = "special_leave_settings_id_seq")
    private Long id;

    private boolean active;
    @NotNull
    private String messageKey;
    @NotNull
    private Integer days;

    protected SpecialLeaveSettingsEntity() {
        // ok
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }
}
