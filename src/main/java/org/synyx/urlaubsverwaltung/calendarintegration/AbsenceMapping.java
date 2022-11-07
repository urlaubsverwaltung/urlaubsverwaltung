package org.synyx.urlaubsverwaltung.calendarintegration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;

/**
 * Mapping object between absence (application for leave or sick note) and sync calendar event.
 */
@Deprecated(since = "4.26.0", forRemoval = true)
@Entity
public class AbsenceMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer absenceId;

    @Enumerated(STRING)
    @Column(nullable = false)
    private AbsenceMappingType absenceMappingType;

    @Column(nullable = false)
    private String eventId;

    protected AbsenceMapping() {
        /* OK */
    }

    public AbsenceMapping(Integer absenceId, AbsenceMappingType absenceMappingType, String eventId) {
        this.absenceId = absenceId;
        this.absenceMappingType = absenceMappingType;
        this.eventId = eventId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAbsenceId() {
        return absenceId;
    }

    public void setAbsenceId(Integer absenceId) {
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
