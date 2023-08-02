package org.synyx.urlaubsverwaltung.calendarintegration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Objects;

import static javax.persistence.EnumType.STRING;

/**
 * Mapping object between absence (application for leave or sick note) and sync calendar event.
 */
@Deprecated(since = "4.26.0", forRemoval = true)
@Entity
public class AbsenceMapping {

    @Id
    @GeneratedValue
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
