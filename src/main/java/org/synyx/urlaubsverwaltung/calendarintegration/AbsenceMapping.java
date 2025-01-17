package org.synyx.urlaubsverwaltung.calendarintegration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;

/**
 * Mapping object between absence (application for leave or sick note) and sync calendar event.
 */
@Entity
public class AbsenceMapping extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "absence_mapping_generator")
    @SequenceGenerator(name = "absence_mapping_generator", sequenceName = "absence_mapping_id_seq")
    private Long id;

    @Column(nullable = false)
    private Long absenceId;

    @Enumerated(STRING)
    @Column(nullable = false)
    private AbsenceMappingType absenceMappingType;

    @Column(nullable = false)
    private String eventId;

    protected AbsenceMapping() {
        /* OK */
    }

    public AbsenceMapping(Long absenceId, AbsenceMappingType absenceMappingType, String eventId) {
        this.absenceId = absenceId;
        this.absenceMappingType = absenceMappingType;
        this.eventId = eventId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAbsenceId() {
        return absenceId;
    }

    public void setAbsenceId(Long absenceId) {
        this.absenceId = absenceId;
    }

    public AbsenceMappingType getAbsenceMappingType() {
        return absenceMappingType;
    }

    public void setAbsenceType(AbsenceMappingType absenceMappingType) {
        this.absenceMappingType = absenceMappingType;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AbsenceMapping that = (AbsenceMapping) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
