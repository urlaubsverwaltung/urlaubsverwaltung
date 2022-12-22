package org.synyx.urlaubsverwaltung.application.specialleave;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity(name = "special_leave_settings")
class SpecialLeaveSettingsEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "special_leaves_settings_generator")
    @SequenceGenerator(name = "special_leaves_settings_generator", sequenceName = "special_leaves_settings_id_seq")
    private Long id;

    private boolean active;
    private String messageKey;
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
